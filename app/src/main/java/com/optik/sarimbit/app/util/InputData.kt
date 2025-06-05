package com.optik.sarimbit.app.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.optik.sarimbit.app.model.Product
import com.optik.sarimbit.app.model.Store
import com.optik.sarimbit.app.model.VariantModel

class InputData {
    val SUNGLASSES = "Sunglasses"
    val KACA_MATA_BACA = "Kaca Mata Baca"
    val DESIGNER_FRAME = "Designer Frames"
    val FASHION_FRAME = "Kacamata Fashion"
    val UNISEX = "Unisex"
    val SEMI_RIMLESS = "Semi Rimless"
    val SPORT_FRAME = "Sport Frames"
    val CAT_EYE = "Cat Eye"
    val WOMEN_FRAME = "Kacamata Wanita"
    val ROUND_FRAME = "Kacamata Round"
    val GEOMETRIC_FRAME = "Kacamata Geometric"
    val OTHERS = "Lainnya"
    fun insertData(activity: BaseView) {
        val firestore = FireStoreUtil(activity)
        val store1 = Store(
            "Optik Sarimbit Ciledug",
            "10.00- 20.30",
            "", "",
            " Optik Sarimbit, Jl. Raden Fatah Jl. H. Umar No.22, RT.1/RW.010, Parung Serab, Ciledug, Tangerang City, Banten 15151",
            "senin - minggu",
            "https://g.co/kgs/My6Yk1W",
            "Tangerang"
        )
        val store2 = Store(
            "Optik Sarimbit Plaza Jambu Dua",
            "09.00 - 20.00",
            "(0251) 8327758", "",
            "Jambu Dua Plaza Blok A1 No.2&3 Lt. Semi Basement Jalan Ahmad Yani Bogor Utara, Tanah Sereal, Bogor City, West Java 16164\n",
            "senin - minggu",
            "https://g.co/kgs/gdqyFq8",
            "Bogor"
        )
        val store6 = Store(
            "Optik Sarimbit RSUD Bogor",
            "09.00- 16.00",
            "0822-9957-1956", "",
            "Jl. DR. Sumeru No.120, RT.02/RW.09, Menteng, Kec. Bogor Baru., Kota Bogor, Jawa Barat 16111",
            "senin - sabtu (setiap hari minggu tutup)",
            "https://g.co/kgs/gdqyFq8",
            "Bogor"
        )
        val store3 = Store(
            "Optik Sarimbit Ciawi",
            "09.00- 21.00",
            "0821-1365-7000", "",
            "sebrang RSUD ciawi, Bendungan, Ciawi, Bogor Regency, West Java",
            "senin - minggu",
            "https://g.co/kgs/ftveJ4X",
            "Bogor"
        )
        val store4 = Store(
            "Optik Prikasih",
            "09.00 - 20.00",
            "0858-9279-3060", "",
            "Jl. Rs Fatmawati no.74 Pondok Labu",
            "senin - sabtu (setiap hari minggu tutup)",
            "https://g.co/kgs/8hXoaXm",
            "Jakarta"

        )
        val store5 = Store(
            "Optik Sarimbit Boyolali",
            "09.00 - 20.00",
            "0822-6532-2023", "",
            "Jln raya bandara No.Rt01/02, Tanjungsari, Ngesrep, Ngemplak, Boyolali Regency, Central Java",
            "senin - sabtu (setiap hari minggu tutup)",
            "https://g.co/kgs/GrDGwh8",
            "Boyolali"
        )
        val data = listOf<Store>(store1, store2, store3, store4, store5, store6)

        data.forEach { it ->
            val dataHashmap: HashMap<String, String> = HashMap()
            dataHashmap.put(Constants.STORE_NAME, it.name)
            dataHashmap.put(Constants.STORE_HOURS, it.hours)
            dataHashmap.put(Constants.STORE_PHONE, it.phone)
            dataHashmap.put(Constants.STORE_IMAGE_URL, it.imageUrl)
            dataHashmap.put(Constants.STORE_ADDRESS, it.address)
            dataHashmap.put(Constants.STORE_DATE, it.date)
            dataHashmap.put(Constants.STORE_GMAPS, it.linkGmaps)
            dataHashmap.put(Constants.STORE_CITY, it.city)
            firestore.insertDataWithoutId(
                "",
                Constants.TABLE_STORE,
                dataHashmap,
                object : CallbackFireStore.TransactionData {
                    override fun onSuccessTransactionData() {
                        Log.d("insert data", "success")

                    }

                    override fun onFailedTransactionData(message: String?) {
                        Log.d("insert data", "failed $message")

                    }

                })
        }
    }

    fun insertDataProduct() {
        val product1 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FGIORDANO%201206.jpg?alt=media&token=874bbb40-1236-4ea0-a8f3-d9ae0fb1d70c",
            88000, "",
            "GIORDANO 1206", "GIORDANO",
            "Description Kaca mata ",
            "round",
            arrayListOf("Demi", "Hitam"),
            arrayListOf(SEMI_RIMLESS, KACA_MATA_BACA,FASHION_FRAME, UNISEX),
            arrayListOf(
                VariantModel(88000, "Frame"),
                VariantModel(163000, "Blueray"),
                VariantModel(198000, "Photo Cromic"),
                VariantModel(249000, "Bluecromic")
            ),
            2
        )
        val product2 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FGIORDANO%203576.jpg?alt=media&token=390ba7d7-9ccc-4087-a70f-b242d1d560ee",
            88000, "",
            "GIORDANO 3576", "GIORDANO",
            "Description Kaca mata ",
            "round",
            arrayListOf("Pink", "Transparent"),
            arrayListOf(SEMI_RIMLESS, KACA_MATA_BACA,FASHION_FRAME, UNISEX),
            arrayListOf(
                VariantModel(88000, "Frame"),
                VariantModel(163000, "Blueray"),
                VariantModel(198000, "Photo Cromic"),
                VariantModel(249000, "Bluecromic")
            ),
            2
        )
        val product3 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2Fgiordano%20eyewear%20cat%20eye%202027.jpg?alt=media&token=55db5baa-167a-4a20-824a-61b2484817c7",
            88000, "",
            "GIORDANO 2027", "GIORDANO",
            "Description Kaca mata ",
            "round",
            arrayListOf("Ungu"  ),
            arrayListOf(WOMEN_FRAME, CAT_EYE,FASHION_FRAME),
            arrayListOf(
                VariantModel(88000, "Frame"),
                VariantModel(163000, "Blueray"),
                VariantModel(198000, "Photo Cromic"),
                VariantModel(249000, "Bluecromic")
            ),
            2
        )
        val product4 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2Fgiordano%20eyewear%20cat%20eye%202369.jpg?alt=media&token=0d50d197-2827-4104-8991-5d702aede034",
            88000, "",
            "GIORDANO 2369", "GIORDANO",
            "Description Kaca mata ",
            "round",
            arrayListOf("Hitam"  ),
            arrayListOf(WOMEN_FRAME, CAT_EYE,FASHION_FRAME),
            arrayListOf(
                VariantModel(88000, "Frame"),
                VariantModel(163000, "Blueray"),
                VariantModel(198000, "Photo Cromic"),
                VariantModel(249000, "Bluecromic")
            ),
            2
        )
        val product5 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2Fgiordano%20eyewear%20cat%20eye%208842.jpg?alt=media&token=aab4b832-ca2d-4e55-b5d5-eba59ad2fa5c",
            88000, "",
            "GIORDANO 8842", "GIORDANO",
            "Description Kaca mata ",
            "round",
            arrayListOf("Hijau", "Transparent"  ),
            arrayListOf(WOMEN_FRAME, CAT_EYE,FASHION_FRAME),
            arrayListOf(
                VariantModel(88000, "Frame"),
                VariantModel(163000, "Blueray"),
                VariantModel(198000, "Photo Cromic"),
                VariantModel(249000, "Bluecromic")
            ),
            2
        )
        val product6 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FPaul%20Frank%20PFF8258%20.jpg?alt=media&token=1e5e2b59-d218-444a-8aaf-7eb90089be23",
            450000, "",
            "PAUL FRANK PFF8258", "PAUL FRANK",
            "Description Kaca mata ",
            "round",
            arrayListOf("Hitam", "Dark Purple"  ),
            arrayListOf(WOMEN_FRAME, CAT_EYE,FASHION_FRAME),
            arrayListOf(
                VariantModel(450000, "Frame"),
                VariantModel(780000, "Blueray"),
                VariantModel(648000, "Photo Cromic"),
                VariantModel(740000, "Bluecromic")
            ),
            2
        )
        val product7 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FPAUL%20FRANK%208208.jpg?alt=media&token=a3c3478c-1b20-4f07-9080-ce3712d1dfa9",
            450000, "",
            "PAUL FRANK 8208", "PAUL FRANK",
            "Description Kaca mata ",
            "round",
            arrayListOf("Brown"),
            arrayListOf(ROUND_FRAME, KACA_MATA_BACA,FASHION_FRAME),
            arrayListOf(
                VariantModel(450000, "Frame"),
                VariantModel(780000, "Blueray"),
                VariantModel(648000, "Photo Cromic"),
                VariantModel(740000, "Bluecromic")
            ),
            2
        )
        val product8 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FBOSSINI%208012.jpg?alt=media&token=6574cf37-a20f-4465-9275-c71e86ae55fe",
            120000, "",
            "BOSSINI 8012", "BOSSINI",
            "Description Kaca mata ",
            "round",
            arrayListOf("Pink"),
            arrayListOf(ROUND_FRAME, KACA_MATA_BACA,FASHION_FRAME),
            arrayListOf(
                VariantModel(120000, "Frame"),
                VariantModel(233000, "Blueray"),
                VariantModel(248000, "Photo Cromic"),
                VariantModel(299000, "Bluecromic")
            ),
            2
        )
        val product9 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FGIORDANO%202310.jpg?alt=media&token=aee2d338-3c58-400c-9b11-0c59e621c8c6",
            88000, "",
            "GIORDANO 2310", "GIORDANO",
            "Description Kaca mata ",
            "round",
            arrayListOf("Biru Transparent"  ),
            arrayListOf(GEOMETRIC_FRAME, KACA_MATA_BACA,FASHION_FRAME),
            arrayListOf(
                VariantModel(88000, "Frame"),
                VariantModel(163000, "Blueray"),
                VariantModel(198000, "Photo Cromic"),
                VariantModel(249000, "Bluecromic")
            ),
            2
        )
        val product10 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FPAUL%20FRANK%208236.jpg?alt=media&token=405cd95d-693f-4f16-9903-83701354e1b5",
            450000, "",
            "PAUL FRANK 8236", "PAUL FRANK",
            "Description Kaca mata ",
            "round",
            arrayListOf("Hitam","Silver"),
            arrayListOf(GEOMETRIC_FRAME, KACA_MATA_BACA,FASHION_FRAME),
            arrayListOf(
                VariantModel(450000, "Frame"),
                VariantModel(780000, "Blueray"),
                VariantModel(648000, "Photo Cromic"),
                VariantModel(740000, "Bluecromic")
            ),
            2
        )
        val product11 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FPaul%20Frank%20PFF%208144.jpg?alt=media&token=11358939-59ef-4c8f-bd71-1cd1108fee1a",
            450000, "",
            "PAUL FRANK 8144", "PAUL FRANK",
            "Description Kaca mata ",
            "round",
            arrayListOf("Grey","Silver"),
            arrayListOf(GEOMETRIC_FRAME, KACA_MATA_BACA,FASHION_FRAME),
            arrayListOf(
                VariantModel(450000, "Frame"),
                VariantModel(780000, "Blueray"),
                VariantModel(648000, "Photo Cromic"),
                VariantModel(740000, "Bluecromic")
            ),
            2
        )
        val product12 = Product(
            "https://firebasestorage.googleapis.com/v0/b/optik-serambit.firebasestorage.app/o/Foto%20Produk%2FPAUL%20FRANK%20PFF8231.jpg?alt=media&token=2b7ed6c0-493c-456a-ae7d-deb48ade34cb",
            450000, "",
            "PAUL FRANK 8231", "PAUL FRANK",
            "Description Kaca mata ",
            "round",
            arrayListOf("Black"),
            arrayListOf(GEOMETRIC_FRAME, KACA_MATA_BACA,FASHION_FRAME),
            arrayListOf(
                VariantModel(450000, "Frame"),
                VariantModel(780000, "Blueray"),
                VariantModel(648000, "Photo Cromic"),
                VariantModel(740000, "Bluecromic")
            ),
            2
        )
        val data = listOf(product1,
            product2,
            product3,
            product4,
            product5,
            product6,
            product7,
            product8,
            product9,
            product10,
            product11,
            product12,
            )
        val db = FirebaseFirestore.getInstance()
        data.forEach { productData ->
            val productRef = db.collection("products") // ⬅️ Simpan dalam koleksi "products"

            productRef.add(productData)
                .addOnSuccessListener { documentReference ->
                    val productId = documentReference.id // 🔥 Dapatkan ID dokumen yang dibuat
                    println("✅ Product added with ID: $productId")
                }
                .addOnFailureListener { e ->
                    println("❌ Error adding product: ${e.message}")
                }
        }
    }
}