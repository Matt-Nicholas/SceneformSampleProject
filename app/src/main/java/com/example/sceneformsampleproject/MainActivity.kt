package com.example.sceneformsampleproject

import ModelLoader
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.content_main.*
import java.lang.ref.WeakReference


class MainActivity : AppCompatActivity() {
    private lateinit var fragment: ArFragment
    private lateinit var modelLoader: ModelLoader

    private  var isTracking = false
    private var isHitting = false

    private  val pointer = PointerDrawable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragment = sceneform_fragment as ArFragment
        modelLoader = ModelLoader(WeakReference(this))

        fragment.arSceneView.scene.addOnUpdateListener { frameTime ->
            fragment.onUpdate(frameTime)
            onUpdate()
        }

        initializeGallery()
    }

    private fun onUpdate() {
        val trackingChanged = updateTracking()
        val contentView = findViewById<View>(android.R.id.content)
        if (trackingChanged) {
            if (isTracking) {
                contentView.overlay.add(pointer)
            } else {
                contentView.overlay.remove(pointer)
            }
            contentView.invalidate()
        }

        if (isTracking) {
            val hitTestChanged = updateHitTest()
            if (hitTestChanged) {
                pointer.setEnabled(isHitting)
                contentView.invalidate()
            }
        }
    }

    private fun updateHitTest(): Boolean {
        val frame = fragment.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        val wasHitting = isHitting
        isHitting = false
        if (frame != null) {
            hits = frame!!.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && (trackable as Plane).isPoseInPolygon(hit.hitPose)) {
                    isHitting = true
                    break
                }
            }
        }
        return wasHitting != isHitting
    }

    private fun getScreenCenter(): android.graphics.Point {
        val vw = findViewById<View>(android.R.id.content)
        return android.graphics.Point(vw.width / 2, vw.height / 2)
    }

    private fun updateTracking(): Boolean {
        val frame = fragment.arSceneView.arFrame
        val wasTracking = isTracking
        isTracking = frame != null && frame.camera.trackingState === TrackingState.TRACKING
        return isTracking != wasTracking
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initializeGallery() {
        val gallery = findViewById<LinearLayout>(R.id.gallery_layout)

        val andy = ImageView(this)
        andy.setImageResource(R.drawable.droid_thumb)
        andy.contentDescription = "andy"
        andy.setOnClickListener { view -> addObject(Uri.parse("andy_dance.sfb")) }
        gallery.addView(andy)

        val cabin = ImageView(this)
        cabin.setImageResource(R.drawable.cabin_thumb)
        cabin.contentDescription = "cabin"
        cabin.setOnClickListener { view -> addObject(Uri.parse("Cabin.sfb")) }
        gallery.addView(cabin)

        val house = ImageView(this)
        house.setImageResource(R.drawable.house_thumb)
        house.contentDescription = "house"
        house.setOnClickListener { view -> addObject(Uri.parse("House.sfb")) }
        gallery.addView(house)

        val igloo = ImageView(this)
        igloo.setImageResource(R.drawable.igloo_thumb)
        igloo.contentDescription = "igloo"
        igloo.setOnClickListener { view -> addObject(Uri.parse("igloo.sfb")) }
        gallery.addView(igloo)
    }

    private fun addObject(model: Uri) {
        val frame = fragment.arSceneView.arFrame
        val pt = getScreenCenter()
        val hits: List<HitResult>
        if (frame != null) {
            hits = frame.hitTest(pt.x.toFloat(), pt.y.toFloat())
            for (hit in hits) {
                val trackable = hit.trackable
                if (trackable is Plane && trackable.isPoseInPolygon(hit.hitPose)) {
                    modelLoader.loadModel(hit.createAnchor(), model)
                    break

                }
            }
        }
    }

    fun addNodeToScene(anchor: Anchor, renderable: ModelRenderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()

        startAnimation(node, renderable)
    }

    fun onException(throwable: Throwable) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(throwable.message)
            .setTitle("Codelab error!")
        val dialog = builder.create()
        dialog.show()
        return
    }

    fun startAnimation(node: TransformableNode, renderable: ModelRenderable){
        if(renderable == null || renderable.animationDataCount == 0){
            return
        }
        for (i in 0 until renderable.animationDataCount){
            var animationData = renderable.getAnimationData(i)
        }
        var animator = ModelAnimator(renderable.getAnimationData(0), renderable)
        animator.start()

        node.setOnTapListener { hitTestResult, motionEvent -> togglePauseAndResume(animator) }
    }

    fun togglePauseAndResume(animator: ModelAnimator){
        if(animator.isPaused){
            animator.resume()
        }
        else if(animator.isStarted){
            animator.pause()
        }
        else{
            animator.start()
        }
    }
}
