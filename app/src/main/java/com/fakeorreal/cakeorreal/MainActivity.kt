package com.fakeorreal.cakeorreal

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
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
    private val rounds = listOf(
        Round("Hyper-realistic chocolate sculpture", "rj60H_Y3bQ8", true, "Amaury Guichon / YouTube Shorts"),
        Round("A real smartphone in use", "https://www.pexels.com/video/a-man-using-a-mobile-phone-7539292/", false, "Pexels stock footage")
    )
    private var roundIndex = 0
    private var score = 0
    private var answered = false
    private lateinit var root: LinearLayout
    private lateinit var scoreLabel: TextView
    private lateinit var questionLabel: TextView
    private lateinit var video: WebView
    private lateinit var answerLabel: TextView
    private lateinit var cakeButton: Button
    private lateinit var realButton: Button
    private lateinit var nextButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(36, 20, 55)
        window.navigationBarColor = Color.rgb(36, 20, 55)
        buildUi()
        loadRound()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(18), dp(16), dp(18), dp(16))
            setBackgroundColor(Color.rgb(36, 20, 55))
        }
        val title = TextView(this).apply {
            text = "🍰 CAKE OR REAL?"
            textSize = 27f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        root.addView(title, matchWrap())
        scoreLabel = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.rgb(255, 219, 111))
            gravity = Gravity.CENTER
            setPadding(0, dp(6), 0, dp(10))
        }
        root.addView(scoreLabel, matchWrap())
        questionLabel = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, dp(10))
        }
        root.addView(questionLabel, matchWrap())
        video = WebView(this).apply {
            setBackgroundColor(Color.BLACK)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = true
            webChromeClient = WebChromeClient()
            webViewClient = WebViewClient()
        }
        root.addView(video, LinearLayout.LayoutParams(-1, 0, 1f))
        answerLabel = TextView(this).apply {
            textSize = 17f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(8))
        }
        root.addView(answerLabel, matchWrap())
        val answerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        cakeButton = makeButton("🍰 It's cake", Color.rgb(255, 105, 145))
        realButton = makeButton("✨ It's real", Color.rgb(91, 205, 160))
        cakeButton.setOnClickListener { answer(true) }
        realButton.setOnClickListener { answer(false) }
        answerRow.addView(cakeButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginEnd = dp(6) })
        answerRow.addView(realButton, LinearLayout.LayoutParams(0, dp(54), 1f).apply { marginStart = dp(6) })
        root.addView(answerRow, matchWrap())
        nextButton = makeButton("Next video →", Color.rgb(255, 202, 87))
        nextButton.setTextColor(Color.rgb(36, 20, 55))
        nextButton.visibility = View.GONE
        nextButton.setOnClickListener {
            roundIndex = (roundIndex + 1) % rounds.size
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
        val item = rounds[roundIndex]
        scoreLabel.text = "Score: $score  •  Round ${roundIndex + 1}/${rounds.size}"
        questionLabel.text = "Is it cake or a real object?"
        answerLabel.text = "Watch the clip, then choose."
        cakeButton.isEnabled = true
        realButton.isEnabled = true
        nextButton.visibility = View.GONE
        if (item.source.startsWith("Pexels")) {
            video.loadUrl(item.videoId)
        } else {
            video.loadDataWithBaseURL(
                "https://www.youtube-nocookie.com",
                """<!doctype html><html><head><meta name="viewport" content="width=device-width,initial-scale=1"></head><body style="margin:0;background:#000"><iframe width="100%" height="100%" style="position:absolute;inset:0;border:0" src="https://www.youtube-nocookie.com/embed/${item.videoId}?playsinline=1&rel=0" title="Cake or Real video" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" allowfullscreen></iframe></body></html>""",
                "text/html", "UTF-8", null
            )
        }
    }

    private fun answer(guessCake: Boolean) {
        if (answered) return
        answered = true
        val item = rounds[roundIndex]
        val correct = guessCake == item.isCake
        if (correct) score++
        scoreLabel.text = "Score: $score  •  Round ${roundIndex + 1}/${rounds.size}"
        val reveal = if (item.isCake) "cake" else "a real object"
        answerLabel.text = if (correct) "🎉 Correct! This clip shows $reveal." else "🙈 Not quite — this clip shows $reveal."
        cakeButton.isEnabled = false
        realButton.isEnabled = false
        nextButton.visibility = View.VISIBLE
        Toast.makeText(this, if (correct) "Correct!" else "Try the next one!", Toast.LENGTH_SHORT).show()
    }

    private fun makeButton(label: String, color: Int): Button = Button(this).apply {
        text = label
        textSize = 14f
        isAllCaps = false
        setTextColor(Color.WHITE)
        setBackgroundColor(color)
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}
