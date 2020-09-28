package com.leptron.leptronquiz.data.remote

import com.leptron.leptronquiz.data.DataError
import com.leptron.leptronquiz.data.QuestionsLocalData
import com.leptron.leptronquiz.data.local.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class QuestionsRemoteRepository internal constructor(
    private val questionsLocal: QuestionsLocalData,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
)    : QuestionsRemoteData {
    private val api: QuestionsAPI

    internal fun defaultClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    init {

        val retrofitCore = Retrofit.Builder()
            .baseUrl("https://opentdb.com")
            .client(defaultClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofitCore.create(QuestionsAPI::class.java)

    }

    override suspend fun refreshQuestions() {
        val result = safeApiCall { api.getQuestions() }

        when(result)
        {
            is ResultWrapper.Success->{
                questionsLocal.deleteAllTasks()
                /*result.value.results.forEach {qa ->
                    questionsLocal.saveQuestion(QuestionHistory(StringUtils.unescapeHTML(qa.question,0), qa.correct_answer))
                }*/
                val list = MutableList<Drink>(0){MyDrink()}
                val OJ = MyJuice()
                OJ.drinkName="Orange Juice"
                OJ.fruitJuice="oranges"
                OJ.isCarbonated=false
                list.add(OJ)
                val Bud = MyBeer()
                Bud.alcoholContent=.05f
                Bud.drinkName="Budweiser"
                Bud.isCarbonated=true
                val pepsi = MyDrink()
                pepsi.drinkName="Pepsi"
                pepsi.isCarbonated=true

                questionsLocal.saveQuestion(
                    QuestionHistory(createString(OJ),"false"))
                questionsLocal.saveQuestion(
                    QuestionHistory(createString(Bud),"false"))
                questionsLocal.saveQuestion(
                    QuestionHistory(createString(pepsi),"false"))

            }
            else->
            {
               //log an error

            }
        }

    }
    private fun createString(drink: Drink) : String
    {
        var carbonated="not Carbonated"
        if(drink.isCarbonated)
        {
            carbonated="Carbonated"
        }
        val result = "${drink.drinkName} $carbonated"
        if(drink is MyJuice)
        {
            return "$result made from ${drink.fruitJuice}"
        }
        if(drink is MyBeer)
        {
            val alcoholContent = String.format( (drink.alcoholContent*100.0f).toString(), "%0.1f")
            return "$result ${alcoholContent}% alcohol"
        }
        return result
    }

    private fun makeRequestBody(json: String): RequestBody {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), json)
    }
    suspend fun <T> safeApiCall( apiCall: suspend () -> T): ResultWrapper<T> {

        try {
            return ResultWrapper.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = convertErrorBody(throwable)
                    return ResultWrapper.NetworkError(code.toString(), errorResponse)
                }
                is UnknownHostException ->
                {
                    return ResultWrapper.NetworkError(DataError.HOST_NOT_FOUND.toString(), throwable.message)
                }
                else -> {
                    return ResultWrapper.NetworkError(DataError.GENERIC.toString(), throwable.message)
                }
            }
        }
    }
    private fun convertErrorBody(throwable: HttpException): String? {
        return throwable.message()
    }
}