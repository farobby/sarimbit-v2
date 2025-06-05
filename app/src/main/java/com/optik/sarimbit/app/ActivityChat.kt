package com.optik.sarimbit.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.LinearLayoutManager
import com.optik.sarimbit.app.adapter.ChatAdapter
import com.optik.sarimbit.app.model.ChatModel
import com.optik.sarimbit.app.util.BaseView
import com.optik.sarimbit.databinding.ActivityChatbotBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivityChat: BaseView() {
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatModel>()
    private lateinit var binding: ActivityChatbotBinding
    private val faqList = listOf(
        "Di mana saja lokasi cabang Optik Sarimbit?",
        "Bagaimana cara memilih frame kacamata sesuai bentuk wajah?",
        "Apa frame kacamata yang cocok untuk wajah kecil?",
        "Apa saja jenis frame kacamata yang tersedia?",
        "Apa saja bahan frame yang sering digunakan?",
        "Apa perbedaan lensa minus, plus, dan silinder?",
        "Apa itu lensa anti radiasi?",
        "Apa itu lensa Blueray?",
        "Apa saja manfaat lensa Blueray?",
        "Apa itu lensa Bluechromic?",
        "Kenapa harus memilih lensa Bluechromic?",
        "Apa itu lensa Photochromic?",
        "Apa keunggulan lensa Photochromic?",
        "Mana yang lebih cocok untuk saya, Blueray, Bluechromic, atau Photochromic?",
        "Apakah di Optik Sarimbit bisa pakai BPJS Kesehatan?",
        "Apakah ada promo atau diskon saat ini?",
        "Bagaimana alur klaim BPJS Kesehatan untuk pembelian kacamata di Optik Sarimbit?",
        "Berapa biaya yang ditanggung BPJS untuk kacamata?"
    )

    private val faqAnswers = mapOf(
        "Di mana saja lokasi cabang Optik Sarimbit?" to "Kami memiliki cabang di:\n\n- Plaza Jambu Dua, Bogor (pusat)\n- RS Ciawi, Bogor\n- RSUD Bogor\n- RS Prikasih, Jakarta Selatan\n- Ciledug, Tangerang\n- Boyolali, Jawa Tengah",
        "Bagaimana cara memilih frame kacamata sesuai bentuk wajah?" to "Tips:\n- **Bulat**: Kotak/persegi panjang\n- **Oval**: Hampir semua model cocok\n- **Kotak**: Bulat/oval\n- **Hati**: Cat-eye atau bawah lebih lebar.",
        "Apa frame kacamata yang cocok untuk wajah kecil?" to "Gunakan frame kecil, medium, rimless, atau half-frame agar lebih proporsional.",
        "Apa saja jenis frame kacamata yang tersedia?" to "1. Full Frame (kokoh)\n2. Half Frame (modern)\n3. Rimless (elegan, ringan)",
        "Apa saja bahan frame yang sering digunakan?" to "1. **Metal**: Kuat, tipis, ringan\n2. **Plastik**: Warna banyak, ringan\n3. **Titanium**: Super ringan, anti karat\n4. **TR90**: Fleksibel, tahan banting",
        "Apa perbedaan lensa minus, plus, dan silinder?" to "1. **Minus**: Rabun jauh\n2. **Plus**: Rabun dekat\n3. **Silinder**: Mata silinder",
        "Apa itu lensa anti radiasi?" to "Lensa ini melindungi mata dari sinar biru gadget, cocok untuk pengguna HP atau komputer.",
        "Apa itu lensa Blueray?" to "Lensa Blueray memiliki lapisan pelindung sinar biru dari gadget, mengurangi kelelahan mata.",
        "Apa saja manfaat lensa Blueray?" to "- Mengurangi mata lelah akibat layar.\n- Melindungi retina dari sinar biru.\n- Membantu tidur lebih nyenyak.",
        "Apa itu lensa Bluechromic?" to "Lensa kombinasi Blueray dan Photochromic. Lindungi dari sinar biru dan otomatis gelap di luar ruangan.",
        "Kenapa harus memilih lensa Bluechromic?" to "- Cocok bagi pengguna gadget dan sering di luar.\n- Lindungi dari sinar biru dan UV.\n- Praktis tanpa harus ganti kacamata.",
        "Apa itu lensa Photochromic?" to "Lensa yang otomatis berubah gelap saat terkena sinar matahari dan kembali bening di dalam ruangan.",
        "Apa keunggulan lensa Photochromic?" to "- Melindungi dari sinar UV.\n- Praktis untuk indoor & outdoor.\n- Mengurangi kelelahan mata di tempat terang.",
        "Mana yang lebih cocok untuk saya, Blueray, Bluechromic, atau Photochromic?" to "Panduan:\n- **Blueray**: Untuk indoor (gadget).\n- **Bluechromic**: Indoor & outdoor.\n- **Photochromic**: Outdoor lebih sering.",
        "Apakah di Optik Sarimbit bisa pakai BPJS Kesehatan?" to "Ya! Kami menerima klaim BPJS Kesehatan dan AdMedika. Bawa kartu BPJS dan resep dokter mata.",
        "Apakah ada promo atau diskon saat ini?" to "Kami sering mengadakan promo! Follow Instagram @Optik_Sarimbit atau cek aplikasi untuk info terbaru.",
        "Bagaimana alur klaim BPJS Kesehatan untuk pembelian kacamata di Optik Sarimbit?" to "1. Periksa mata di Faskes 1 & dapatkan rujukan.\n2. Dokter spesialis akan memberi resep.\n3. Bawa resep & kartu BPJS ke Optik Sarimbit.",
        "Berapa biaya yang ditanggung BPJS untuk kacamata?" to "BPJS menanggung sesuai kelas:\n- **Kelas 1**: Rp 300.000\n- **Kelas 2**: Rp 200.000\n- **Kelas 3**: Rp 150.000\n(Bisa klaim tiap 2 tahun)."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatbotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatAdapter = ChatAdapter { selectedQuestion ->
            sendUserMessage(selectedQuestion)
        }

        binding.recyclerchat.layoutManager = LinearLayoutManager(this)
        binding.recyclerchat.adapter = chatAdapter
        binding.btnStartChat.setOnClickListener { chatLayout() }
        binding.ivClose.setOnClickListener {
            finish()
        }
        noChatLayout()
        showInitialMessages()
    }

    private fun sendUserMessage(question: String) {
        val userMessage = ChatModel(question, getCurrentTime(), true)
        updateMessages(userMessage)

        // Tambahkan bubble "Sedang mengetik..."
        val typingIndicator = ChatModel("Sedang mengetik...", getCurrentTime(), false)
        updateMessages(typingIndicator)

        Handler(Looper.getMainLooper()).postDelayed({
            // Hapus "Sedang mengetik..." sebelum menambahkan jawaban bot
            messages.remove(typingIndicator)

            val botResponse = faqAnswers[question] ?: "Maaf, saya tidak mengerti."
            updateMessages(ChatModel(botResponse, getCurrentTime(), false))

            Handler(Looper.getMainLooper()).postDelayed({
                showInitialQuestions()
            }, 3000) // Tambahkan delay sebelum menampilkan pertanyaan lagi
        }, 1500)
    }

    private fun noChatLayout() {
        binding.civ.showView()
        binding.cvCard.showView()
        binding.llChat.hideView()
    }

    private fun chatLayout() {
        binding.civ.hideView()
        binding.cvCard.hideView()
        binding.llChat.showView()
    }

    private fun showInitialMessages() {
        messages.add(ChatModel("Hello ", "12:31 PM", false))
        messages.add(ChatModel("Welcome to LiveChat! Pilih topik dari daftar atau ketik pertanyaan!", "12:31 PM", false))
        chatAdapter.submitList(messages.toList())
        showInitialQuestions()
    }

    private fun showInitialQuestions() {
        updateMessages(ChatModel(null, getCurrentTime(), false, faqList))
    }

    private fun updateMessages(chatModel: ChatModel) {
        messages.add(chatModel)
        chatAdapter.submitList(messages.toList()) {
            binding.recyclerchat.post {
                binding.recyclerchat.scrollToPosition(messages.size - 1)
            }
        }
    }
    private fun getCurrentTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
}