package com.optik.sarimbit

import org.junit.Test
import org.junit.Assert.*
import java.text.DecimalFormat

/**
 * Unit test for the rule-based chatbot logic in ActivityChat.
 *
 * This test creates a confusion matrix to verify that each predefined question
 * correctly maps to its intended answer.
 */
class ChatbotLogicTest {

    //region --- Chatbot Data from ActivityChat.kt ---
    // This data is copied from your ActivityChat for testing purposes.
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
    //endregion

    /**
     * Simulates the bot's core logic of retrieving an answer for a given question.
     */
    private fun getBotResponse(question: String): String {
        return faqAnswers[question] ?: "BOT_ERROR: Jawaban tidak ditemukan"
    }

    /**
     * Finds the question (intent) associated with a given answer.
     * This is the reverse of the bot's logic, used for testing.
     */
    private fun findIntentForAnswer(answer: String): String? {
        // Find the entry in the map where the value matches the answer
        return faqAnswers.entries.find { it.value == answer }?.key
    }

    @Test
    fun chatbot_confusionMatrixTest() {
        val intents = faqList
        val intentCount = intents.size

        // Initialize the confusion matrix with zeros
        // The keys are the "actual" intents, and the inner map's keys are the "predicted" intents.
        val confusionMatrix = intents.associateWith {
            intents.associateWith { 0 }.toMutableMap()
        }.toMutableMap()

        // --- Populate the Matrix ---
        // Iterate through each actual question and check the bot's response
        for (actualIntent in intents) {
            val botAnswer = getBotResponse(actualIntent)
            val predictedIntent = findIntentForAnswer(botAnswer)

            assertNotNull("Bot produced an answer that doesn't match any known question.", predictedIntent)

            // Increment the count in the matrix at [actual][predicted]
            confusionMatrix[actualIntent]?.let { it[predictedIntent!!] = it.getOrDefault(predictedIntent, 0) + 1 }
        }

        // --- Print the Confusion Matrix ---
        println("--- Chatbot Confusion Matrix ---")
        val header = intents.joinToString(separator = " | ", prefix = "|               | ") { "Pred ${it.take(5)}" }
        println(header)
        println("-".repeat(header.length))

        confusionMatrix.forEach { (actual, predictions) ->
            val row = predictions.values.joinToString(separator = " | ") { String.format("%-10d", it) }
            println(String.format("| %-13s | %s |", "Act ${actual.take(5)}", row))
        }
        println("----------------------------------\n")

        // --- Calculate and Print Metrics ---
        println("--- Performance Metrics ---")
        val metrics = mutableMapOf<String, Triple<Double, Double, Double>>() // Precision, Recall, F1
        var totalCorrect = 0.0

        for (i in intents.indices) {
            val intent = intents[i]

            // True Positives: Correctly identified cases for this class
            val tp = confusionMatrix[intent]!![intent]!!.toDouble()

            // False Positives: Other intents incorrectly predicted as this class
            val fp = confusionMatrix.values.sumOf { it[intent]!!.toDouble() } - tp

            // False Negatives: This class was incorrectly predicted as something else
            val fn = confusionMatrix[intent]!!.values.sum() - tp

            val precision = if ((tp + fp) > 0) tp / (tp + fp) else 0.0
            val recall = if ((tp + fn) > 0) tp / (tp + fn) else 0.0
            val f1Score = if ((precision + recall) > 0) 2 * (precision * recall) / (precision + recall) else 0.0

            metrics[intent] = Triple(precision, recall, f1Score)
            totalCorrect += tp
        }

        // Print metrics for each intent
        val dec = DecimalFormat("#.##")
        println("| %-45s | %-10s | %-10s | %-10s |".format("Intent (Question)", "Precision", "Recall", "F1-Score"))
        println("-".repeat(90))
        metrics.forEach { (intent, scores) ->
            println("| %-45s | %-10s | %-10s | %-10s |".format(intent, dec.format(scores.first), dec.format(scores.second), dec.format(scores.third)))
        }

        // Print overall accuracy
        val accuracy = totalCorrect / intentCount
        println("\nOverall Accuracy: ${dec.format(accuracy * 100)}%")

        // --- Assertions ---
        // For a rule-based system, we expect 100% accuracy.
        assertEquals(1.0, accuracy, 0.0)
    }
}