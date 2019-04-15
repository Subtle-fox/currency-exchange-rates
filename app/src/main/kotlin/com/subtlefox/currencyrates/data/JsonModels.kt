package com.subtlefox.currencyrates.data

import com.google.gson.annotations.SerializedName

class JsonRateModel(
    @SerializedName("base")
    val baseIso: String,

    @SerializedName("rates")
    val rates: Map<String, String>
)

class JsonInfoModel(
    @SerializedName("code")
    val code: String,

    @SerializedName("name")
    val name: String
)