package com.martysh12.racecs.screens

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.io.InputStream

@Environment(EnvType.CLIENT)
abstract class InteractiveImageScreen(
    title: Text,
    protected val client: MinecraftClient,
    val manager: ScreenManager
) : RaceCSAbstractScreen(title) {
    inner class Image : Drawable {
        override fun render(
            context: DrawContext?,
            mouseX: Int,
            mouseY: Int,
            delta: Float
        ) {
            context?.let { ctx ->
                imageTexture?.let { texture ->
                    // Calculate the rendered dimensions
                    val renderedWidth = (imageWidth * scale).toInt()
                    val renderedHeight = (imageHeight * scale).toInt()

                    // Draw the image with current offset and scale
                    ctx.drawTexture(
                        texture,
                        offsetX.toInt(), offsetY.toInt(),
                        0f, 0f,
                        renderedWidth, renderedHeight,
                        renderedWidth, renderedHeight
                    )
                }
            }
        }
    }
    
    private var imageTexture: Identifier? = null
    private var imageWidth = 0
    private var imageHeight = 0
    
    // Pan and zoom state
    protected var offsetX = 0.0
    protected var offsetY = 0.0
    protected var scale = 1.0
    private var isDragging = false
    private var lastMouseX = 0.0
    private var lastMouseY = 0.0
    
    // Configurable zoom limits
    protected var minZoom = 0.1
    protected var maxZoom = 5.0

    private var isSetUp = false;
    private val image = Image()

    /**
     * Must be overridden to provide the Identifier for the image to display
     */
    abstract fun getImageIdentifier(): Identifier

    override fun init() {
        super.init()
        loadImage()

        this.addDrawable(image);

        // Add reset zoom button in bottom right corner
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("screen.race.map.reset_zoom")) { resetZoom() }
                .dimensions(width - 90, height - 30, 80, 20)
                .build()
        )

    }

    private fun loadImage() {
        try {
            val imageId = getImageIdentifier()
            client.resourceManager.getResource(imageId).get().inputStream.use { stream: InputStream ->
                val image = NativeImage.read(stream)
                imageWidth = image.width
                imageHeight = image.height
                imageTexture = client.textureManager.registerDynamicTexture(
                    "interactive_image_${imageId.namespace}_${imageId.path.replace('/', '_')}",
                    NativeImageBackedTexture(image)
                )
            }

            if (!isSetUp) {
                isSetUp = true
                resetZoom()
            }
        } catch (e: Exception) {
            println("Failed to load image: ${e.message}")
        }
    }

    private fun resetZoom() {

        // Calculate scale to fit the window while maintaining aspect ratio
        val windowWidth = width
        val windowHeight = height

        // Calculate scale factors for both dimensions
        val scaleX = windowWidth.toDouble() / imageWidth
        val scaleY = windowHeight.toDouble() / imageHeight

        // Use the smaller scale to ensure the image fits both dimensions
        scale = minOf(scaleX, scaleY) * 0.9 // 0.9 to add a small margin

        // Center the scaled image
        offsetX = (windowWidth - imageWidth * scale) / 2.0
        offsetY = (windowHeight - imageHeight * scale) / 2.0
    }

    override fun render(context: DrawContext?, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(context, mouseX, mouseY, delta)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        // First check if any widgets (like buttons) were clicked
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true
        }
        
        // If no widgets were clicked, handle image dragging
        if (button == 0 && mouseY > 20) { // Left click
            isDragging = true
            lastMouseX = mouseX
            lastMouseY = mouseY
            return true
        }
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            isDragging = false

            return true
        }
        return super.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, deltaX: Double, deltaY: Double): Boolean {
        if (isDragging && button == 0) {
            offsetX += mouseX - lastMouseX
            offsetY += mouseY - lastMouseY
            lastMouseX = mouseX
            lastMouseY = mouseY

            return true
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val zoomFactor = if (verticalAmount > 0) 1.1 else 0.9
        val newScale = scale * zoomFactor
        
        if (newScale in minZoom..maxZoom) {
            // Zoom towards mouse cursor
            val mouseRelativeX = mouseX - offsetX
            val mouseRelativeY = mouseY - offsetY
            
            scale = newScale
            
            // Adjust offset to keep the point under cursor fixed
            offsetX = mouseX - mouseRelativeX * zoomFactor
            offsetY = mouseY - mouseRelativeY * zoomFactor
            
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        val panAmount = 10.0 // Adjust this value to control pan speed

        when (keyCode) {
            // WASD controls
            87, // W key
            265 -> { // Up arrow
                offsetY += panAmount
                return true
            }
            83, // S key
            264 -> { // Down arrow
                offsetY -= panAmount
                return true
            }
            65, // A key
            263 -> { // Left arrow
                offsetX += panAmount
                return true
            }
            68, // D key
            262 -> { // Right arrow
                offsetX -= panAmount
                return true
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers)
    }
}