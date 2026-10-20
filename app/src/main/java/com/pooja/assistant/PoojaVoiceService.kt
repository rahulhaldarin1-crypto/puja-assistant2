package com.pooja.assistant

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale

class PoojaVoiceService : Service(), TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var tts: TextToSpeech? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
        setupSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("bn", "BD")
            speakOut("আমি পূজা, আমি শুনছি। আদেশ করুন।")
        }
    }

    private fun setupSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        handler.postDelayed({ startListening() }, 2000)
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val command = matches[0].lowercase(Locale.ROOT)
                            handleVoiceCommand(command)
                        }
                        handler.postDelayed({ startListening() }, 1500)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
            }

            startListening()
        }
    }

    private fun startListening() {
        try {
            recognizerIntent?.let { speechRecognizer?.startListening(it) }
        } catch (e: Exception) {
            // সার্ভিস ব্যস্ত থাকলে ক্যাচ করবে
        }
    }

    private fun handleVoiceCommand(command: String) {
        Toast.makeText(this, "আপনি বলেছেন: $command", Toast.LENGTH_SHORT).show()

        when {
            command.contains("ইউটিউব") || command.contains("youtube") -> {
                speakOut("ইউটিউব ওপেন করছি")
                openApp("com.google.android.youtube")
            }
            command.contains("হোয়াটসঅ্যাপ") || command.contains("whatsapp") -> {
                speakOut("হোয়াটসঅ্যাপ ওপেন করছি")
                openApp("com.whatsapp")
            }
            command.contains("কেমন আছো") -> {
                speakOut("আমি ভালো আছি, আপনার দিনটি কেমন কাটছে?")
            }
            command.contains("পূজা") || command.contains("puja") -> {
                speakOut("জি, বলুন কিভাবে সাহায্য করতে পারি?")
            }
            else -> {
                speakOut("আমি শুনেছি: $command")
            }
        }
    }

    private fun openApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(launchIntent)
        } else {
            speakOut("অ্যাপটি ফোনে খুঁজে পাওয়া যায়নি")
        }
    }

    private fun speakOut(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
    }
}
