package jm.dev.qrcodescanner

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.vision.barcode.Barcode
import jm.dev.qrcodescanner.databinding.ActivityScanResultBinding

class ScanResult : AppCompatActivity() {

    lateinit var binding: ActivityScanResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val code = intent.getParcelableExtra<Barcode>("code")

        if (code!!.email != null) {
                            binding.tvScanResult.text = code!!.email.address
//                            qrValue = code.email.address
//                            isEmail = true
//                            binding.btnResult.text = "Send Mail"

                        } else {
//                            isEmail = false
                            binding.tvScanResult.text = code!!.displayValue
//                            qrValue = code.displayValue
//                            binding.btnResult.text = "Open URL"
                        }
    }
}