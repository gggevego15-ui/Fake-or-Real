package com.fakeorreal.cakeorreal

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.AudioManager
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.view.Gravity
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    private data class Round(val title: String, val videoId: String, val isCake: Boolean, val source: String)
    // Answer key is explicit per round. Pexels pages below describe ordinary real-world footage.
    private val rounds = listOf(
        Round("Chocolate pencil illusion", "rj60H_Y3bQ8", true, "Amaury Guichon / YouTube Shorts"),
        Round("Chocolate sneaker illusion", "Pz9PA3V9Jzw", true, "Amaury Guichon / verified YouTube"),
        Round("Chocolate palm tree sculpture", "jzIrq-I8lMI", true, "Amaury Guichon / verified YouTube"),
        Round("Pencil, book and sandals — cake illusion", "x9w0sgw", true, "Dailymotion cake-illusion compilation"),
        Round("Hyper-realistic cake cutting reveal", "x84wpdd", true, "Dailymotion cake-illusion video"),
        Round("A real smartphone in use", "https://www.pexels.com/video/a-man-using-a-mobile-phone-7539292/", false, "Pexels stock footage"),
        Round("A real banana close-up", "https://www.pexels.com/video/person-holding-a-banana-7199074/", false, "Pexels stock footage"),
        Round("A real person using a phone with coffee", "https://www.pexels.com/video/a-man-using-his-smartphone-while-holding-a-cup-of-coffee-5630783/", false, "Pexels stock footage"),
        Round("A real cup of coffee while walking", "https://www.pexels.com/video/man-holding-a-cup-of-coffee-while-walking-5321129/", false, "Pexels stock footage"),
        Round("A real laptop and coffee", "https://www.pexels.com/video/a-man-is-holding-a-cup-of-coffee-while-using-a-laptop-20246512/", false, "Pexels stock footage"),
        Round("A real person walking outdoors", "https://www.pexels.com/search/videos/walking/", false, "Pexels stock footage"),
        Round("Real fruit and food close-ups", "https://www.pexels.com/search/videos/fruit/", false, "Pexels stock footage"),
        Round("Real sneakers in motion", "https://www.pexels.com/search/videos/sneakers/", false, "Pexels stock footage"),
        Round("Real desserts on a table", "https://www.pexels.com/search/videos/dessert/", false, "Pexels stock footage")
    )
    private var roundOrder = rounds.shuffled()
    private var roundIndex = 0
    private var score = 0
    private var answered = false
    private var toneGenerator: ToneGenerator? = null
    private var backgroundTrack: AudioTrack? = null
    private var musicEnabled = true
    private lateinit var root: LinearLayout
    private lateinit var scoreLabel: TextView
    private lateinit var questionLabel: TextView
    private lateinit var video: WebView
    private lateinit var answerLabel: TextView
    private lateinit var cakeButton: Button
    private lateinit var realButton: Button
    private lateinit var answerRow: LinearLayout
    private lateinit var nextButton: Button
    private var currentWatchUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(36, 20, 55)
        window.navigationBarColor = Color.rgb(36, 20, 55)
        toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        startBackgroundMusic()
        buildHome()
    }

    private fun buildHome() {
        val home = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(26), dp(28), dp(26), dp(28))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(34, 17, 58), Color.rgb(91, 39, 100), Color.rgb(25, 20, 52)))
        }
        home.addView(TextView(this).apply {
            text = "THE SWEETEST GUESSING GAME"
            textSize = 11f
            letterSpacing = 0.18f
            setTextColor(Color.rgb(255, 219, 145))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(18))
        }, LinearLayout.LayoutParams(-1, -2))
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(18), dp(22), dp(18), dp(22))
            background = rounded(Color.rgb(78, 43, 98), 28, Color.rgb(166, 108, 182), 1)
        }
        hero.addView(TextView(this).apply {
            text = "🍰"
            textSize = 70f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, -2))
        hero.addView(TextView(this).apply {
            text = "CAKE OR REAL?"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, -2))
        hero.addView(TextView(this).apply {
            text = "Looks real. Might be cake."
            textSize = 15f
            setTextColor(Color.rgb(255, 219, 145))
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        }, LinearLayout.LayoutParams(-1, -2))
        home.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(18) })
        home.addView(TextView(this).apply {
            text = "CAN YOU SPOT THE ILLUSION?"
            textSize = 14f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(12))
        }, LinearLayout.LayoutParams(-1, -2))
        val featureRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        featureRow.addView(homeFeature("🎬", "VIDEO\nROUNDS"), LinearLayout.LayoutParams(0, dp(76), 1f).apply { marginEnd = dp(5) })
        featureRow.addView(homeFeature("🧠", "TRICKY\nILLUSIONS"), LinearLayout.LayoutParams(0, dp(76), 1f).apply { marginStart = dp(5); marginEnd = dp(5) })
        featureRow.addView(homeFeature("🏆", "BEAT YOUR\nSCORE"), LinearLayout.LayoutParams(0, dp(76), 1f).apply { marginStart = dp(5) })
        home.addView(featureRow, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(18) })
        home.addView(makeButton("▶  PLAY", Color.rgb(255, 202, 87)).apply {
            setTextColor(Color.rgb(36, 20, 55))
            textSize = 19f
            setOnClickListener { startGame() }
        }, LinearLayout.LayoutParams(-1, dp(60)).apply { bottomMargin = dp(12) })
        home.addView(musicToggleButton(), LinearLayout.LayoutParams(-1, dp(46)).apply { topMargin = dp(10) })
        home.addView(makeButton("HOW TO PLAY  ⓘ", Color.rgb(91, 205, 160)).apply {
            setOnClickListener {
                Toast.makeText(this@MainActivity, "1. Watch the video.  2. Guess CAKE or REAL.  3. Earn points for every correct answer!", Toast.LENGTH_LONG).show()
            }
        }, LinearLayout.LayoutParams(-1, dp(54)))
        home.addView(TextView(this).apply {
            text = "14 ROUNDS  •  RANDOM ORDER  •  FREE TO PLAY"
            textSize = 10f
            letterSpacing = 0.06f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(20), 0, 0)
        }, LinearLayout.LayoutParams(-1, -2))
        setContentView(home)
    }

    private fun startGame() {
        score = 0
        roundOrder = rounds.shuffled()
        roundIndex = 0
        answered = false
        buildUi()
        loadRound()
    }

    private fun returnHome() {
        if (::video.isInitialized) {
            video.stopLoading()
            video.loadUrl("about:blank")
        }
        buildHome()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(18), dp(18), dp(16))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(34, 17, 58), Color.rgb(69, 31, 83), Color.rgb(25, 20, 52))
            )
        }
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(18), dp(16), dp(18))
            background = rounded(Color.rgb(78, 43, 98), 24, Color.rgb(135, 91, 157), 1)
        }
        val title = TextView(this).apply {
            text = "🍰  CAKE OR REAL?"
            textSize = 28f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            gravity = Gravity.CENTER
            letterSpacing = 0.03f
        }
        brand.addView(title, matchWrap())
        val subtitle = TextView(this).apply {
            text = "CAN YOU SPOT THE ILLUSION?"
            textSize = 11f
            setTextColor(Color.rgb(255, 219, 145))
            typeface = Typeface.DEFAULT_BOLD
            letterSpacing = 0.16f
            gravity = Gravity.CENTER
            setPadding(0, dp(7), 0, 0)
        }
        brand.addView(subtitle, matchWrap())
        root.addView(brand, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(8) })
        root.addView(TextView(this).apply {
            text = "‹  MAIN MENU"
            textSize = 12f
            setTextColor(Color.rgb(255, 219, 145))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(10), dp(3), dp(10), dp(3))
            setOnClickListener { returnHome() }
        }, LinearLayout.LayoutParams(-1, dp(26)))
        root.addView(musicToggleButton(), LinearLayout.LayoutParams(-1, dp(38)).apply { bottomMargin = dp(8) })
        scoreLabel = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.rgb(255, 232, 178))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(Color.rgb(48, 31, 68), 18, Color.rgb(102, 74, 123), 1)
        }
        root.addView(scoreLabel, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        questionLabel = TextView(this).apply {
            textSize = 19f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(4), dp(4), dp(12))
        }
        root.addView(questionLabel, matchWrap())
        video = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.loadsImagesAutomatically = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url.toString()
                    if (url.startsWith("intent:")) {
                        try { startActivity(Intent.parseUri(url, Intent.URI_INTENT_SCHEME)) } catch (_: Exception) { }
                        return true
                    }
                    return false
                }
            }
        }
        video.background = rounded(Color.BLACK, 22, Color.rgb(166, 108, 182), 2)
        video.clipToOutline = true
        root.addView(video, LinearLayout.LayoutParams(-1, 0, 1f).apply {
            bottomMargin = dp(5)
        })
        val openVideoButton = makeButton("▶  WATCH VIDEO IN BROWSER", Color.rgb(91, 205, 160)).apply {
            setTextColor(Color.rgb(25, 35, 34))
            setOnClickListener {
                try { startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(currentWatchUrl))) }
                catch (_: Exception) { Toast.makeText(this@MainActivity, "Couldn't open video link", Toast.LENGTH_SHORT).show() }
            }
        }
        root.addView(openVideoButton, LinearLayout.LayoutParams(-1, dp(42)).apply { bottomMargin = dp(8) })
        answerLabel = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = rounded(Color.rgb(48, 31, 68), 16, Color.rgb(102, 74, 123), 1)
        }
        root.addView(answerLabel, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })
        answerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        cakeButton = makeButton("🍰 It's cake", Color.rgb(255, 105, 145))
        realButton = makeButton("✨ It's real", Color.rgb(91, 205, 160))
        root.addView(answerRow, matchWrap())
        nextButton = makeButton("Next video →", Color.rgb(255, 202, 87))
        nextButton.setTextColor(Color.rgb(36, 20, 55))
        nextButton.visibility = View.GONE
        nextButton.setOnClickListener {
            it.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(90).start()
            }.start()
            roundIndex++
            if (roundIndex >= roundOrder.size) {
                roundOrder = rounds.shuffled()
                roundIndex = 0
            }
            answered = false
            loadRound()
        }
        root.addView(nextButton, LinearLayout.LayoutParams(-1, dp(50)).apply { topMargin = dp(8) })
        val foot = TextView(this).apply {
            text = "Video playback needs an internet connection"
            textSize = 11f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, 0)
        }
        root.addView(foot, matchWrap())
        setContentView(root)
    }

    private fun loadRound() {
        val item = roundOrder[roundIndex]
        scoreLabel.text = "Score: $score  •  Round ${roundIndex + 1}/${rounds.size}"
        questionLabel.text = "Is it cake or a real object?"
        answerLabel.text = "Watch the clip, then choose."
        answerLabel.alpha = 0f
        answerLabel.animate().alpha(1f).setDuration(300).start()
        cakeButton.isEnabled = true
        realButton.isEnabled = true
        // Randomize the physical order of the two answer choices on every round.
        answerRow.removeAllViews()
        cakeButton.setOnClickListener { animateTap(it); answer(true) }
        realButton.setOnClickListener { animateTap(it); answer(false) }
        val cakeFirst = listOf(true, false).shuffled().first()
        if (cakeFirst) {
            answerRow.addView(cakeButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginEnd = dp(6) })
            answerRow.addView(realButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginStart = dp(6) })
        } else {
            answerRow.addView(realButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginEnd = dp(6) })
            answerRow.addView(cakeButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginStart = dp(6) })
        }
        nextButton.visibility = View.GONE
        val isPexels = item.source.startsWith("Pexels")
        val isDailymotion = item.source.startsWith("Dailymotion")
        currentWatchUrl = when {
            isPexels -> item.videoId
            isDailymotion -> "https://www.dailymotion.com/video/${item.videoId}"
            else -> "https://www.youtube.com/watch?v=${item.videoId}"
        }
        // Some providers block embedded playback inside Android WebView.
        // Try the provider page directly and offer a browser fallback above.
        video.loadUrl(currentWatchUrl)
    }

    private fun startBackgroundMusic() {
        if (backgroundTrack != null || !musicEnabled) return
        try {
            val sampleRate = 22050
            val seconds = 8
            val sampleCount = sampleRate * seconds
            val pcm = ShortArray(sampleCount)
            // Original soft ambient loop, synthesized locally without a music download.
            val chords = arrayOf(
                doubleArrayOf(261.63, 329.63, 392.00),
                doubleArrayOf(220.00, 261.63, 329.63),
                doubleArrayOf(174.61, 220.00, 261.63),
                doubleArrayOf(196.00, 246.94, 293.66)
            )
            for (i in 0 until sampleCount) {
                val t = i.toDouble() / sampleRate
                val chord = chords[(t.toInt() / 2) % chords.size]
                var sample = 0.0
                for (freq in chord) sample += kotlin.math.sin(2.0 * Math.PI * freq * t) * 0.075
                val beat = t % 1.0
                val noteFreq = chord[t.toInt() % 3]
                sample += kotlin.math.sin(2.0 * Math.PI * noteFreq * t) * kotlin.math.exp(-beat * 4.2) * 0.12
                val fade = kotlin.math.min(1.0, kotlin.math.min(t / 0.25, (seconds - t) / 0.25))
                pcm[i] = (sample * fade.coerceAtLeast(0.0) * 32767.0).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            val track = AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, pcm.size * 2, AudioTrack.MODE_STATIC)
            track.write(pcm, 0, pcm.size)
            track.setLoopPoints(0, pcm.size, -1)
            track.setVolume(0.22f)
            track.play()
            backgroundTrack = track
        } catch (_: Exception) {
            stopBackgroundMusic()
        }
    }

    private fun stopBackgroundMusic() {
        try { backgroundTrack?.pause(); backgroundTrack?.flush(); backgroundTrack?.release() } catch (_: Exception) { }
        backgroundTrack = null
    }

    private fun musicToggleButton(): Button = makeButton(if (musicEnabled) "♫  MUSIC: ON" else "♫  MUSIC: OFF", Color.rgb(91, 205, 160)).apply {
        setOnClickListener {
            musicEnabled = !musicEnabled
            if (musicEnabled) startBackgroundMusic() else stopBackgroundMusic()
            text = if (musicEnabled) "♫  MUSIC: ON" else "♫  MUSIC: OFF"
        }
    }

    private fun answer(guessCake: Boolean) {
        if (answered) return
        answered = true
        val item = roundOrder[roundIndex]
        val correct = guessCake == item.isCake
        if (correct) score++
        scoreLabel.text = "Score: $score  •  Round ${roundIndex + 1}/${rounds.size}"
        val reveal = if (item.isCake) "cake" else "a real object"
        answerLabel.text = if (correct) "🎉 Correct! This clip shows $reveal." else "🙈 Not quite — this clip shows $reveal."
        toneGenerator?.startTone(if (correct) ToneGenerator.TONE_PROP_ACK else ToneGenerator.TONE_PROP_NACK, 180)
        answerLabel.alpha = 0f
        answerLabel.scaleX = 0.85f
        answerLabel.scaleY = 0.85f
        answerLabel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(260).start()
        scoreLabel.animate().cancel()
        scoreLabel.scaleX = 0.9f
        scoreLabel.scaleY = 0.9f
        scoreLabel.animate().scaleX(1f).scaleY(1f).setDuration(220).start()
        cakeButton.isEnabled = false
        realButton.isEnabled = false
        nextButton.alpha = 0f
        nextButton.visibility = View.VISIBLE
        nextButton.animate().alpha(1f).setDuration(250).start()
        Toast.makeText(this, if (correct) "Correct!" else "Try the next one!", Toast.LENGTH_SHORT).show()
    }

    private fun animateTap(view: View) {
        view.animate().scaleX(0.94f).scaleY(0.94f).setDuration(70).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(100).start()
        }.start()
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (::video.isInitialized) returnHome() else super.onBackPressed()
    }

    override fun onDestroy() {
        toneGenerator?.release()
        toneGenerator = null
        stopBackgroundMusic()
        if (::video.isInitialized) video.destroy()
        super.onDestroy()
    }

    private fun homeFeature(emoji: String, label: String): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER
        setPadding(dp(4), dp(8), dp(4), dp(8))
        background = rounded(Color.rgb(55, 34, 73), 16, Color.rgb(102, 74, 123), 1)
        addView(TextView(this@MainActivity).apply {
            text = emoji
            textSize = 22f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(-1, -2))
        addView(TextView(this@MainActivity).apply {
            text = label
            textSize = 9f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(3), 0, 0)
        }, LinearLayout.LayoutParams(-1, -2))
    }

    private fun makeButton(label: String, color: Int): Button = Button(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        typeface = Typeface.DEFAULT_BOLD
        letterSpacing = 0.01f
        setTextColor(if (color == Color.rgb(255, 202, 87)) Color.rgb(36, 20, 55) else Color.WHITE)
        background = rounded(color, 17, Color.argb(70, 255, 255, 255), 1)
        elevation = dp(3).toFloat()
        stateListAnimator = null
    }

    private fun rounded(fill: Int, radiusDp: Int, stroke: Int, strokeDp: Int): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(radiusDp).toFloat()
            setStroke(dp(strokeDp), stroke)
        }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
