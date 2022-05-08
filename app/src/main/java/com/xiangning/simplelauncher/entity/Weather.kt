package com.xiangning.simplelauncher.entity
import com.google.gson.annotations.SerializedName


data class WeatherResponse(
    @SerializedName("data")
    val `data`: Data? = null,
    @SerializedName("desc")
    val desc: String = "",
    @SerializedName("status")
    val status: Int = 0
) {
    data class Data(
        @SerializedName("city")
        val city: String = "",
        @SerializedName("forecast")
        val forecast: List<Forecast> = listOf(),
        @SerializedName("ganmao")
        val ganmao: String = "",
        @SerializedName("wendu")
        val wendu: String = "",
        @SerializedName("yesterday")
        val yesterday: Yesterday = Yesterday()
    ) {
        data class Forecast(
            @SerializedName("date")
            val date: String = "",
            @SerializedName("fengli")
            val fengli: String = "",
            @SerializedName("fengxiang")
            val fengxiang: String = "",
            @SerializedName("high")
            val high: String = "",
            @SerializedName("low")
            val low: String = "",
            @SerializedName("type")
            val type: String = ""
        )

        data class Yesterday(
            @SerializedName("date")
            val date: String = "",
            @SerializedName("fl")
            val fl: String = "",
            @SerializedName("fx")
            val fx: String = "",
            @SerializedName("high")
            val high: String = "",
            @SerializedName("low")
            val low: String = "",
            @SerializedName("type")
            val type: String = ""
        )
    }
}