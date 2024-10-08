/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ylabz.aifitmeal.data

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.floor
import kotlin.random.Random

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Demonstrates reading and writing from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

    var availability = mutableStateOf(HealthConnectAvailability.NOT_SUPPORTED)
        private set

    init {
        checkAvailability()
    }

    private fun checkAvailability() {
        availability.value = when {
            HealthConnectClient.isProviderAvailable(context) -> HealthConnectAvailability.INSTALLED
            isSupported() -> HealthConnectAvailability.NOT_INSTALLED
            else -> HealthConnectAvailability.NOT_SUPPORTED
        }
    }

    /**
     * Determines whether all the specified permissions are already granted. It is recommended to
     * call [PermissionController.getGrantedPermissions] first in the permissions flow, as if the
     * permissions are already granted then there is no need to request permissions via
     * [PermissionController.createRequestPermissionResultContract].
     */
    suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
        return healthConnectClient.permissionController.getGrantedPermissions()
            .containsAll(permissions)
    }

    fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
        return PermissionController.createRequestPermissionResultContract()
    }

    /**
     * TODO: Writes [WeightRecord] to Health Connect.
     */
    suspend fun writeWeightInput(weightInput: Double) {
        val time = ZonedDateTime.now().withNano(0)
        val weightRecord = WeightRecord(
            weight = Mass.kilograms(weightInput),
            time = time.toInstant(),
            zoneOffset = time.offset
        )
        val records = listOf(weightRecord)
        try {
            healthConnectClient.insertRecords(records)
            Toast.makeText(context, "Successfully insert records", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, e.message.toString(), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * TODO: Reads in existing [WeightRecord]s.
     */
    suspend fun readWeightInputs(start: Instant, end: Instant): List<WeightRecord> {
        val request = ReadRecordsRequest(
            recordType = WeightRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    /**
     * TODO: Returns the weekly average of [WeightRecord]s.
     */
    suspend fun computeWeeklyAverage(start: Instant, end: Instant): Mass? {
        val request = AggregateRequest(
            metrics = setOf(WeightRecord.WEIGHT_AVG),
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.aggregate(request)
        return response[WeightRecord.WEIGHT_AVG]
    }

    /**
     * TODO: Obtains a list of [ExerciseSessionRecord]s in a specified time frame. An Exercise Session Record is a
     * period of time given to an activity, that would make sense to a user, e.g. "Afternoon run"
     * etc. It does not necessarily mean, however, that the user was *running* for that entire time,
     * more that conceptually, this was the activity being undertaken.
     */
    suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(start, end)
        )
        val response = healthConnectClient.readRecords(request)
        return response.records
    }

    /**
     * TODO: Writes an [ExerciseSessionRecord] to Health Connect.
     */
    suspend fun writeExerciseCalSession(start: ZonedDateTime, end: ZonedDateTime) {
        healthConnectClient.insertRecords(
            listOf(
                TotalCaloriesBurnedRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    energy = Energy.calories((140 + Random.nextInt(20)) * 0.01)
                )
            ) + buildHeartRateSeries(start, end)
        )
    }


    /**
     * TODO: Writes an [ExerciseSessionRecord] to Health Connect.
     */
    suspend fun writeExerciseSession(start: ZonedDateTime, end: ZonedDateTime) {
        healthConnectClient.insertRecords(
            listOf(
                ExerciseSessionRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_RUNNING,
                    title = "My Run #${Random.nextInt(0, 60)}"
                ),
                StepsRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    count = (1000 + 1000 * Random.nextInt(3)).toLong()
                ),
                TotalCaloriesBurnedRecord(
                    startTime = start.toInstant(),
                    startZoneOffset = start.offset,
                    endTime = end.toInstant(),
                    endZoneOffset = end.offset,
                    energy = Energy.calories((140 + Random.nextInt(20)) * 0.01)
                )
            ) + buildHeartRateSeries(start, end)
        )
    }

    /**
     * TODO: Build [HeartRateRecord].
     */
    private fun buildHeartRateSeries(
        sessionStartTime: ZonedDateTime,
        sessionEndTime: ZonedDateTime,
    ): HeartRateRecord {
        val samples = mutableListOf<HeartRateRecord.Sample>()
        var time = sessionStartTime
        while (time.isBefore(sessionEndTime)) {
            samples.add(
                HeartRateRecord.Sample(
                    time = time.toInstant(),
                    beatsPerMinute = (80 + Random.nextInt(80)).toLong()
                )
            )
            time = time.plusSeconds(30)
        }
        return HeartRateRecord(
            startTime = sessionStartTime.toInstant(),
            startZoneOffset = sessionStartTime.offset,
            endTime = sessionEndTime.toInstant(),
            endZoneOffset = sessionEndTime.offset,
            samples = samples
        )

    }

    /**
     * TODO: Reads aggregated data and raw data for selected data types, for a given [ExerciseSessionRecord].
     */
    suspend fun readAssociatedSessionData(
        uid: String,
    ): ExerciseSessionData {
        val exerciseSession = healthConnectClient.readRecord(ExerciseSessionRecord::class, uid)
        // Use the start time and end time from the session, for reading raw and aggregate data.
        val timeRangeFilter = TimeRangeFilter.between(
            startTime = exerciseSession.record.startTime,
            endTime = exerciseSession.record.endTime
        )
        val aggregateDataTypes = setOf(
            ExerciseSessionRecord.EXERCISE_DURATION_TOTAL,
            StepsRecord.COUNT_TOTAL,
            TotalCaloriesBurnedRecord.ENERGY_TOTAL,
        )
        // Limit the data read to just the application that wrote the session. This may or may not
        // be desirable depending on the use case: In some cases, it may be useful to combine with
        // data written by other apps.
        val dataOriginFilter = setOf(exerciseSession.record.metadata.dataOrigin)
        val aggregateRequest = AggregateRequest(
            metrics = aggregateDataTypes,
            timeRangeFilter = timeRangeFilter,
            dataOriginFilter = dataOriginFilter
        )
        val aggregateData = healthConnectClient.aggregate(aggregateRequest)
        val heartRateData = readData<HeartRateRecord>(timeRangeFilter, dataOriginFilter)

        return ExerciseSessionData(
            uid = uid,
            totalActiveTime = aggregateData[ExerciseSessionRecord.EXERCISE_DURATION_TOTAL],
            totalSteps = aggregateData[StepsRecord.COUNT_TOTAL],
            totalEnergyBurned = aggregateData[TotalCaloriesBurnedRecord.ENERGY_TOTAL],
            minHeartRate = aggregateData[HeartRateRecord.BPM_MIN],
            maxHeartRate = aggregateData[HeartRateRecord.BPM_MAX],
            avgHeartRate = aggregateData[HeartRateRecord.BPM_AVG],
            heartRateSeries = heartRateData,
        )
    }

    /**
     * Obtains a changes token for the specified record types.
     */
    suspend fun getChangesToken(): String {
        return healthConnectClient.getChangesToken(
            ChangesTokenRequest(
                setOf(
                    ExerciseSessionRecord::class,
                    StepsRecord::class,
                    TotalCaloriesBurnedRecord::class,
                    HeartRateRecord::class,
                    WeightRecord::class
                )
            )
        )
    }

    /**
     * Retrieve changes from a changes token.
     */
    suspend fun getChanges(token: String): Flow<ChangesMessage> = flow {
        var nextChangesToken = token
        do {
            val response = healthConnectClient.getChanges(nextChangesToken)
            if (response.changesTokenExpired) {
                // As described here: https://developer.android.com/guide/health-and-fitness/health-connect/data-and-data-types/differential-changes-api
                // tokens are only valid for 30 days. It is important to check whether the token has
                // expired. As well as ensuring there is a fallback to using the token (for example
                // importing data since a certain date), more importantly, the app should ensure
                // that the changes API is used sufficiently regularly that tokens do not expire.
                throw IOException("Changes token has expired")
            }
            emit(ChangesMessage.ChangeList(response.changes))
            nextChangesToken = response.nextChangesToken
        } while (response.hasMore)
        emit(ChangesMessage.NoMoreChanges(nextChangesToken))
    }

    /**
     * Convenience function to reuse code for reading data.
     */
    private suspend inline fun <reified T : Record> readData(
        timeRangeFilter: TimeRangeFilter,
        dataOriginFilter: Set<DataOrigin> = setOf(),
    ): List<T> {
        val request = ReadRecordsRequest(
            recordType = T::class,
            dataOriginFilter = dataOriginFilter,
            timeRangeFilter = timeRangeFilter
        )
        return healthConnectClient.readRecords(request).records
    }



    suspend fun getTotalCaloriesBurnedDebug(): String? = withContext(Dispatchers.IO) {
        try {
            val systemZoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(systemZoneId)

            // Start of the current day
            val todayStart = now.toLocalDate().atStartOfDay(systemZoneId)

            // Query for the time range from the start of today to now
            val timeRangeFilter = TimeRangeFilter.between(
                todayStart.toInstant(),
                now.toInstant()
            )

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(systemZoneId)
            Log.d("HealthConnectManager", "todayStart (System Default): ${todayStart.format(formatter)}")
            Log.d("HealthConnectManager", "now (System Default): ${now.format(formatter)}")
            Log.d("HealthConnectManager", "Time Zone: System Default")

            // Read ExerciseSessionRecords
            val exerciseReadRequest = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = timeRangeFilter
            )

            val exerciseReadResponse = healthConnectClient.readRecords(exerciseReadRequest)

            // Read TotalCaloriesBurnedRecords
            val caloriesReadRequest = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = timeRangeFilter
            )

            val caloriesReadResponse = healthConnectClient.readRecords(caloriesReadRequest)
            var totalCalories = 0.0

            val walkingSessions = exerciseReadResponse.records.filter {
                it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_WALKING
            }

            for (record in caloriesReadResponse.records) {
                // Check if the calories burned record overlaps with any walking session
                val isDuringWalking = walkingSessions.any { session ->
                    (record.startTime in session.startTime..session.endTime) ||
                            (record.endTime in session.startTime..session.endTime) ||
                            (record.startTime <= session.startTime && record.endTime >= session.endTime)
                }

                if (isDuringWalking) {
                    val dataOrigin = record.metadata.dataOrigin?.packageName ?: "Unknown source"
                    val startTime = record.startTime.atZone(systemZoneId).format(formatter)
                    val endTime = record.endTime.atZone(systemZoneId).format(formatter)
                    val adjustedCalories = record.energy.inCalories / 1000.0 // Adjust calories

                    Log.d("HealthConnectManager", "Included Record: start time = $startTime, end time = $endTime, calories (adjusted) = $adjustedCalories, data origin = $dataOrigin")

                    totalCalories += adjustedCalories
                } else {
                    Log.d("HealthConnectManager", "Excluded Record: start time = ${record.startTime.atZone(systemZoneId).format(formatter)}, end time = ${record.endTime.atZone(systemZoneId).format(formatter)}, calories (adjusted) = ${record.energy.inCalories / 1000.0}")
                }
            }

            Log.d("HealthConnectManager", "Total calories from today start to now (adjusted): $totalCalories")

            Math.round(totalCalories).toString()
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error fetching calories data", e)
            null
        }
    }



    suspend fun getTotalCaloriesBurnedByWalking(): String? = withContext(Dispatchers.IO) {
        try {
            val systemZoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(systemZoneId)

            // The timestamp marks the very start of the current day in the system default time zone
            val todayStart = now.toLocalDate().atStartOfDay(systemZoneId)

            // Query for the time range from the start of today to now
            val timeRangeFilter = TimeRangeFilter.between(
                todayStart.toInstant(),
                now.toInstant()
            )

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(systemZoneId)
            Log.d("HealthConnectManager", "todayStart (System Default): ${todayStart.format(formatter)}")
            Log.d("HealthConnectManager", "now (System Default): ${now.format(formatter)}")
            Log.d("HealthConnectManager", "Time Zone: System Default")

            // Read ExerciseSessionRecords
            val exerciseReadRequest = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = timeRangeFilter
            )

            val exerciseReadResponse = healthConnectClient.readRecords(exerciseReadRequest)

            // Filter for walking sessions and get start and end times
            val walkingSessions = exerciseReadResponse.records.filter {
                it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_WALKING
            }.map {
                it.startTime..it.endTime
            }

            // Read TotalCaloriesBurnedRecords
            val caloriesReadRequest = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = timeRangeFilter
            )

            val caloriesReadResponse = healthConnectClient.readRecords(caloriesReadRequest)
            var totalCalories = 0.0

            for (record in caloriesReadResponse.records) {
                // Check if the calories burned record overlaps with any walking session
                val isDuringWalking = walkingSessions.any { range ->
                    record.startTime in range || record.endTime in range
                }

                if (isDuringWalking) {
                    val dataOrigin = record.metadata.dataOrigin?.packageName ?: "Unknown source"
                    val startTime = record.startTime.atZone(systemZoneId).format(formatter)
                    val endTime = record.endTime.atZone(systemZoneId).format(formatter)
                    val adjustedCalories = record.energy.inCalories / 1000.0 // Adjust calories

                    Log.d("HealthConnectManager", "Record: start time = $startTime, end time = $endTime, calories (adjusted) = $adjustedCalories, data origin = $dataOrigin")

                    totalCalories += adjustedCalories
                }
            }

            Log.d("HealthConnectManager", "Total calories in the last 24 hours (adjusted): $totalCalories")

            Math.round(totalCalories).toString()
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error fetching calories data", e)
            null
        }
    }




    suspend fun getTotalCaloriesBurned24hours(): String? = withContext(Dispatchers.IO) {
        try {
            val systemZoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(systemZoneId)

            // Calculate the timestamp for 24 hours ago
            val yesterday = now.minusHours(24)

            // Query for the last 24 hours in the system default time zone
            val timeRangeFilter = TimeRangeFilter.between(
                yesterday.toInstant(),
                now.toInstant()
            )

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(systemZoneId)
            Log.d("HealthConnectManager", "yesterday (System Default): ${yesterday.format(formatter)}")
            Log.d("HealthConnectManager", "now (System Default): ${now.format(formatter)}")
            Log.d("HealthConnectManager", "Time Zone: System Default")

            val readRequest = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = timeRangeFilter
            )

            val readResponse = healthConnectClient.readRecords(readRequest)
            var totalCalories = 0.0

            for (record in readResponse.records) {
                val dataOrigin = record.metadata.dataOrigin?.packageName ?: "Unknown source"
                val startTime = record.startTime.atZone(systemZoneId).format(formatter)
                val endTime = record.endTime.atZone(systemZoneId).format(formatter)
                val adjustedCalories = record.energy.inCalories / 1000.0 // Adjust calories

                Log.d("HealthConnectManager", "Record: start time = $startTime, end time = $endTime, calories (adjusted) = $adjustedCalories, data origin = $dataOrigin")

                totalCalories += adjustedCalories
            }

            Log.d("HealthConnectManager", "Total calories in the last 24 hours (adjusted): $totalCalories")

            Math.round(totalCalories).toString()
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error fetching calories data", e)
            null
        }
    }


    suspend fun getTotalCaloriesBurnedNow(): String? = withContext(Dispatchers.IO) {
        try {
            val systemZoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(systemZoneId)

            // The timestamp marks the very start of the current day in the system default time zone
            val todayStart = now.toLocalDate().atStartOfDay(systemZoneId)

            // Query for the time range from the start of today to now
            val timeRangeFilter = TimeRangeFilter.between(
                todayStart.toInstant(),
                now.toInstant()
            )

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(systemZoneId)
            Log.d("HealthConnectManager", "todayStart (System Default): ${todayStart.format(formatter)}")
            Log.d("HealthConnectManager", "now (System Default): ${now.format(formatter)}")
            Log.d("HealthConnectManager", "Time Zone: System Default")

            val readRequest = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = timeRangeFilter
            )

            val readResponse = healthConnectClient.readRecords(readRequest)
            var totalCalories = 0.0

            for (record in readResponse.records) {
                val dataOrigin = record.metadata.dataOrigin?.packageName ?: "Unknown source"
                val startTime = record.startTime.atZone(systemZoneId).format(formatter)
                val endTime = record.endTime.atZone(systemZoneId).format(formatter)
                val adjustedCalories = record.energy.inCalories / 1000.0 // Adjust calories

                Log.d("HealthConnectManager", "Record: start time = $startTime, end time = $endTime, calories (adjusted) = $adjustedCalories, data origin = $dataOrigin")

                totalCalories += adjustedCalories
            }

            Log.d("HealthConnectManager", "Total calories from today start to now (adjusted): $totalCalories")

            Math.round(totalCalories).toString()
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error fetching calories data", e)
            null
        }
    }


    suspend fun getTotalCaloriesBurnedYesterday(): String? = withContext(Dispatchers.IO) {
        try {
            val systemZoneId = ZoneId.systemDefault() // Use system default time zone
            val now = LocalDate.now(systemZoneId)

            // The timestamp marks the very start of the current day in the system default time zone
            val todayStart = now.atStartOfDay(systemZoneId)
            val startYesterday = todayStart.minusDays(1)

            // Query for the last 24 hours in the system default time zone
            val timeRangeFilter = TimeRangeFilter.between(
                startYesterday.toInstant(),
                todayStart.toInstant()
            )

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(systemZoneId)
            Log.d("HealthConnectManager", "yesterday (System Default): ${startYesterday.format(formatter)}")
            Log.d("HealthConnectManager", "todayStart (System Default): ${todayStart.format(formatter)}")
            Log.d("HealthConnectManager", "Time Zone: System Default")

            val readRequest = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = timeRangeFilter
            )

            val readResponse = healthConnectClient.readRecords(readRequest)
            var totalCalories = 0.0

            for (record in readResponse.records) {
                val dataOrigin = record.metadata.dataOrigin?.packageName ?: "Unknown source"
                val startTime = record.startTime.atZone(systemZoneId).format(formatter)
                val endTime = record.endTime.atZone(systemZoneId).format(formatter)
                val adjustedCalories = record.energy.inCalories / 1000.0 // Divide calories by 1,000

                Log.d("HealthConnectManager", "Record: start time = $startTime, end time = $endTime, calories (adjusted) = $adjustedCalories, data origin = $dataOrigin")

                totalCalories += adjustedCalories
            }

            val totalSeconds = 24 * 60 * 60 // Total seconds in a day
            val averageCaloriesPerSecond = totalCalories / totalSeconds

            Log.d("HealthConnectManager", "Total calories in the last 24 hours (adjusted): $totalCalories")
            Log.d("HealthConnectManager", "Average calories per second (adjusted): $averageCaloriesPerSecond")

            Math.round(totalCalories).toString()
        } catch (e: Exception) {
            Log.e("HealthConnectManager", "Error fetching calories data", e)
            null
        }
    }




    private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

    // Represents the two types of messages that can be sent in a Changes flow.
    sealed class ChangesMessage {
        data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
        data class ChangeList(val changes: List<Change>) : ChangesMessage()
    }
}


/**
 * Health Connect requires that the underlying Health Connect APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
    INSTALLED,
    NOT_INSTALLED,
    NOT_SUPPORTED
}
