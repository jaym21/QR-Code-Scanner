package jm.dev.qrcodescanner

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.vision.barcode.Barcode
import jm.dev.qrcodescanner.databinding.ActivityScanResultBinding

class ScanResult : AppCompatActivity() {

    lateinit var binding: ActivityScanResultBinding
    var isEmail: Boolean = false
    private var qrValue: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val code = intent.getParcelableExtra<Barcode>("code")

        if (code!!.email != null) {
            binding.tvScanResult.text = code!!.email.address
            qrValue = code.email.address
            isEmail = true
            binding.btnScanResult.text = "Send Mail"

        } else {
            isEmail = false
            binding.tvScanResult.text = code!!.displayValue
            qrValue = code.displayValue
            binding.btnScanResult.text = "Open URL"
        }


        binding.btnScanResult.setOnClickListener {
            //checking if some qrCode is detected when button is pressed
            if (qrValue.isNotEmpty()) {
                //checking if detected code is an email
                if (isEmail) {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(Intent.EXTRA_EMAIL, qrValue)
                    intent.type = "message/rfc822"
                    startActivity(Intent.createChooser(intent, "Select email"))
                } else {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(qrValue)))
                }
            }
        }

        binding.btnShare.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.putExtra(Intent.EXTRA_TEXT, qrValue)
            shareIntent.type = "text/plain"
            startActivity(shareIntent)
        }
    }
}