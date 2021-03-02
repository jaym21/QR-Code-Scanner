package jm.dev.qrcodescanner

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import jm.dev.qrcodescanner.databinding.ActivityMainBinding
import java.lang.Exception
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val REQUESTCODE_CAMERA = 1000
    lateinit var detector: BarcodeDetector
    lateinit var cameraSource: CameraSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //checking if camera permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        }

        setup()
    }

    private fun setup() {
        //initializing barcode detector
        detector = BarcodeDetector.Builder(this).build()
        //initializing camera source
        cameraSource = CameraSource.Builder(this, detector)
            .setAutoFocusEnabled(true)
            .build()

        //setting up surfaceView
        binding.svCamera.holder.addCallback(surfaceCallback)

        //setting the processor for detector
        detector.setProcessor(processor)
    }

    //making surfaceCallback to setup surfaceView
    private val surfaceCallback = object: SurfaceHolder.Callback {
        @SuppressLint("MissingPermission")
        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            try {
                //starting the camera as soon as activity is created
                cameraSource.start(surfaceHolder)
            }catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

        }

        override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
            cameraSource.stop()
        }

    }

    //making processor for detector
    private val processor = object: Detector.Processor<Barcode> {
        override fun release() {

        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            //checking if the detection from detector is not empty i.e some barcode is detected in camera in surfaceView
            if (detections != null && detections.detectedItems.isNotEmpty()) {
                //storing detected codes in sparseArray
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)
                binding.tvResult.text = code.displayValue
            } else {
                //when no barcode is detected in the camera in surfaceView
                binding.tvResult.text = ""
            }
        }

    }

    //requesting camera permission
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), REQUESTCODE_CAMERA)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUESTCODE_CAMERA && grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setup()
            } else {
                Toast.makeText(applicationContext, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }
}