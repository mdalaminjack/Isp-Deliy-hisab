package com.example.dailyhisab

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import android.graphics.Typeface
import android.graphics.Color
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Entry(val id: Long, val kind: String, val category: String, val note: String, val amount: Double, val date: String)

class HisabDb(ctx: android.content.Context) : SQLiteOpenHelper(ctx, "daily_hisab.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""CREATE TABLE entries (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            kind TEXT NOT NULL, category TEXT NOT NULL, note TEXT,
            amount REAL NOT NULL, date TEXT NOT NULL)""")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    fun add(kind: String, category: String, note: String, amount: Double, date: String) {
        val v = android.content.ContentValues().apply {
            put("kind", kind); put("category", category); put("note", note)
            put("amount", amount); put("date", date)
        }
        writableDatabase.insert("entries", null, v)
    }
    fun all(): List<Entry> {
        val list = mutableListOf<Entry>()
        readableDatabase.rawQuery("SELECT id,kind,category,note,amount,date FROM entries ORDER BY date DESC,id DESC", null).use { c ->
            while (c.moveToNext()) list.add(Entry(c.getLong(0), c.getString(1), c.getString(2), c.getString(3) ?: "", c.getDouble(4), c.getString(5)))
        }
        return list
    }
    fun delete(id: Long) { writableDatabase.delete("entries", "id=?", arrayOf(id.toString())) }
    fun totals(): Triple<Double, Double, Double> {
        var income=0.0; var expense=0.0
        readableDatabase.rawQuery("SELECT kind, SUM(amount) FROM entries GROUP BY kind", null).use { c ->
            while(c.moveToNext()) {
                if(c.getString(0)=="আয়") income=c.getDouble(1) else expense=c.getDouble(1)
            }
        }
        return Triple(income, expense, income-expense)
    }
}

class MainActivity : Activity() {
    private lateinit var db: HisabDb
    private lateinit var root: LinearLayout
    private lateinit var summary: TextView
    private lateinit var listBox: LinearLayout
    private val green = Color.rgb(23,107,87)
    private val dark = Color.rgb(25,45,40)
    private val muted = Color.rgb(100,115,109)
    private fun money(n: Double) = "৳ " + String.format(Locale.US, "%,.2f", n)
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = HisabDb(this)
        buildUi()
        refresh()
    }

    private fun buildUi() {
        root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(246,249,247)) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(18),dp(18),dp(18),dp(24)) }
        val title = TextView(this).apply {
            text="দৈনিক হিসাব"; textSize=26f; setTextColor(dark); typeface=Typeface.DEFAULT_BOLD
        }
        content.addView(title)
        content.addView(TextView(this).apply { text="আপনার ব্যক্তিগত আয় ও ব্যয়ের সহজ হিসাব"; textSize=14f; setTextColor(muted); setPadding(0,dp(4),0,dp(16)) })
        summary = TextView(this).apply {
            textSize=15f; setTextColor(Color.WHITE); setPadding(dp(16),dp(16),dp(16),dp(16))
            setBackgroundColor(green)
        }
        content.addView(summary, LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(16) })
        val add = Button(this).apply {
            text="＋ নতুন আয় / ব্যয় যোগ করুন"; setTextColor(Color.WHITE); setBackgroundColor(green)
            setOnClickListener { showAddDialog() }
        }
        content.addView(add, LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(18) })
        content.addView(TextView(this).apply {
            text="লেনদেনের তালিকা"; textSize=19f; setTextColor(dark); typeface=Typeface.DEFAULT_BOLD
            setPadding(0,0,0,dp(10))
        })
        listBox = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        content.addView(listBox)
        scroll.addView(content)
        root.addView(scroll)
        setContentView(root)
    }

    private fun refresh() {
        val t=db.totals()
        summary.text = "মোট আয়     ${money(t.first)}\nমোট ব্যয়   ${money(t.second)}\n────────────────────\nঅবশিষ্ট      ${money(t.third)}"
        listBox.removeAllViews()
        val entries=db.all()
        if(entries.isEmpty()) {
            listBox.addView(TextView(this).apply {
                text="এখনো কোনো হিসাব যোগ করা হয়নি।\nউপরের বোতামে চাপ দিয়ে প্রথম হিসাব যোগ করুন।"
                textSize=14f; setTextColor(muted); setPadding(dp(14),dp(18),dp(14),dp(18))
            })
        }
        entries.forEach { e ->
            val card=LinearLayout(this).apply {
                orientation=LinearLayout.VERTICAL; setPadding(dp(14),dp(12),dp(14),dp(12))
                setBackgroundColor(Color.WHITE)
            }
            val top=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL }
            val left=TextView(this).apply {
                text="${e.category}  •  ${e.date}\n${e.note}".trim()
                textSize=14f; setTextColor(dark)
            }
            top.addView(left, LinearLayout.LayoutParams(0,-2,1f))
            val amount=TextView(this).apply {
                text=(if(e.kind=="আয়") "+" else "−")+money(e.amount)
                textSize=15f; typeface=Typeface.DEFAULT_BOLD
                setTextColor(if(e.kind=="আয়") green else Color.rgb(190,65,55))
            }
            top.addView(amount)
            card.addView(top)
            card.setOnLongClickListener {
                AlertDialog.Builder(this).setTitle("হিসাব মুছবেন?")
                    .setMessage("${e.category} — ${money(e.amount)}")
                    .setNegativeButton("না",null)
                    .setPositiveButton("মুছুন") { _,_ -> db.delete(e.id); refresh() }.show()
                true
            }
            listBox.addView(card, LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(8) })
        }
        if(entries.isNotEmpty()) listBox.addView(TextView(this).apply {
            text="কোনো হিসাব মুছতে সেটির ওপর কিছুক্ষণ চেপে ধরুন।"
            textSize=12f; setTextColor(muted); setPadding(0,dp(4),0,0)
        })
    }

    private fun showAddDialog() {
        val panel=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(dp(20),dp(8),dp(20),0) }
        val type=Spinner(this)
        type.adapter=ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("আয়","ব্যয়"))
        panel.addView(label("লেনদেনের ধরন")); panel.addView(type)
        val incomeCats=listOf("বেতন","ব্যবসা থেকে আয়","ফ্রিল্যান্স/অনলাইন আয়","উপহার","অন্যান্য আয়")
        val expenseCats=listOf("খাবার","বাজার","বাসা ভাড়া","যাতায়াত","বিদ্যুৎ/গ্যাস/পানি","চিকিৎসা","শিক্ষা","বিনোদন","কেনাকাটা","অন্যান্য ব্যয়")
        val category=Spinner(this)
        fun updateCats() {
            val items=if(type.selectedItemPosition==0) incomeCats else expenseCats
            category.adapter=ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        }
        type.onItemSelectedListener=object: android.widget.AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) { updateCats() }
        }
        panel.addView(label("ক্যাটাগরি")); panel.addView(category)
        val amount=EditText(this).apply { hint="টাকার পরিমাণ"; inputType= android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        panel.addView(label("পরিমাণ (৳)")); panel.addView(amount)
        val note=EditText(this).apply { hint="বিবরণ (ঐচ্ছিক)" }
        panel.addView(label("বিবরণ")); panel.addView(note)
        val date=EditText(this).apply {
            setText(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date()))
            hint="তারিখ: YYYY-MM-DD"; isFocusable=false
        }
        panel.addView(label("তারিখ")); panel.addView(date)
        val dialog=AlertDialog.Builder(this).setTitle("নতুন হিসাব যোগ করুন").setView(panel)
            .setNegativeButton("বাতিল",null)
            .setPositiveButton("সংরক্ষণ",null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val value=amount.text.toString().trim().toDoubleOrNull()
                if(value==null || value<=0) {
                    amount.error="সঠিক টাকার পরিমাণ লিখুন"; return@setOnClickListener
                }
                val kind=if(type.selectedItemPosition==0) "আয়" else "ব্যয়"
                db.add(kind, category.selectedItem.toString(), note.text.toString().trim(), value, date.text.toString())
                dialog.dismiss(); refresh()
            }
        }
        dialog.show()
    }

    private fun label(s:String)=TextView(this).apply {
        text=s; textSize=13f; setTextColor(muted); setPadding(0,dp(10),0,dp(2))
    }
}
