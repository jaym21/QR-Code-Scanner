package jm.dev.qrcodescanner

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isNotEmpty
import com.google.android.gms.dynamic.IFragmentWrapper
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
    lateinit var cameraManager: CameraManager
    lateinit var cameraId: String
    var qrValue: String = ""
    var isEmail: Boolean = false
    var isFlashOn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //checking if camera permission is granted
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        }

        //checking if flash is available on device
        val isFlashAvailable = applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

        if (!isFlashAvailable) {
            Toast.makeText(applicationContext, "Flash not available", Toast.LENGTH_SHORT).show()
            //disabling flash button if not available
            binding.btnFlash.isEnabled = false
        }


        binding.btnFlash.setOnClickListener {
            isFlashOn = !isFlashOn


            if (isFlashOn) {
                binding.btnFlash.background = ContextCompat.getDrawable(this, R.drawable.ic_flash_on)
            }else {
                binding.btnFlash.background = ContextCompat.getDrawable(this, R.drawable.ic_flash_off)
            }
            switchFlashLight(isFlashOn)
        }

        //starting detector camera
        setupCameraDetector()

        binding.btnResult.setOnClickListener {
            //checking if some qrCode is detected when button is pressed
            if (qrValue.isNotEmpty()) {
                //checking if detected code is an email
                if (isEmail) {
                    val intent = Intent(Intent.ACTION_SENDTO)
                    intent.putExtra(Intent.EXTRA_EMAIL, qrValue)
                    startActivity(intent)
                }else {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(qrValue)))
                }
            }
        }
    }

    private fun switchFlashLight(isFlashOn: Boolean) {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager.cameraIdList[0]
            cameraManager.setTorchMode(cameraId, isFlashOn)
        }catch (e: CameraAccessException) {
            e.printStackTrace()
        }


    }

    private fun setupCameraDetector() {
        //initializing barcode detector
        detector = BarcodeDetector.Builder(this)
//                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build()
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

        override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
            try {
                //starting the camera as soon as activity is created
                if (ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraSource.start(surfaceHolder)
                }else {
                    requestCameraPermission()
                }

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
            Toast.makeText(applicationContext, "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
        }

        override fun receiveDetections(detections: Detector.Detections<Barcode>) {
            //checking if the detection from detector is not empty i.e some barcode is detected in camera in surfaceView
            if (detections != null && detections.detectedItems.isNotEmpty()) {
                //storing detected codes in sparseArray
                val qrCodes: SparseArray<Barcode> = detections.detectedItems
                val code = qrCodes.valueAt(0)


                if(qrCodes.size() != 0) {
                        if (code.email != null) {
                            binding.tvResult.text = code.email.address
                            qrValue = code.email.address
                            isEmail = true
                            binding.btnResult.text = "Send Mail"

                        } else {
                            isEmail = false
                            binding.tvResult.text = code.displayValue
                            qrValue = code.displayValue
                            binding.btnResult.text = "Open URL"
                        }
                }

            } else {
                //when no barcode is detected in the camera in surfaceView
                binding.tvResult.text = "No Barcode Detected"
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
                setupCameraDetector()
            } else {
                Toast.makeText(applicationContext, "Permission not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupCameraDetector()
    }

    override fun onPause() {
        super.onPause()
        cameraSource.release()
    }
}