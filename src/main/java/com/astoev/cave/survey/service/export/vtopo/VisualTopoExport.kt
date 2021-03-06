package com.astoev.cave.survey.service.export.vtopo

import android.content.Context
import android.util.Log
import com.astoev.cave.survey.Constants
import com.astoev.cave.survey.activity.map.MapUtilities
import com.astoev.cave.survey.model.Location
import com.astoev.cave.survey.model.Option.*
import com.astoev.cave.survey.model.Photo
import com.astoev.cave.survey.model.Project
import com.astoev.cave.survey.model.Sketch
import com.astoev.cave.survey.service.Options.getOptionValue
import com.astoev.cave.survey.service.export.AbstractExport
import com.astoev.cave.survey.service.export.AbstractExport.Entities.*
import com.astoev.cave.survey.service.export.ExportEntityType
import com.astoev.cave.survey.service.export.ExportEntityType.*
import com.astoev.cave.survey.service.gps.UtmCoordinate
import com.astoev.cave.survey.util.AndroidUtil
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*

class VisualTopoExport(aContext: Context?) : AbstractExport(aContext) {

    val SEPARATOR = ","
    val COORDINATE_PLACEHOLDER = ",,,,LT93";
    val ENTRANCE = "Entree A0"
    val PLACEHOLDER = "*"

    private var body = StringBuilder()
    private var rowType: ExportEntityType? = null
    private var location: String = COORDINATE_PLACEHOLDER
    private var legFrom: String = "A0"
    private var entrance: String = ENTRANCE
    private var distanceInMeters = UNIT_METERS.equals(getOptionValue(CODE_DISTANCE_UNITS))

    init {
        mUseUniqueName = true;
        mExtension = ".tro";
        body.clear()
    }

    override fun setValue(entityType: Entities, value: String) {
        val entry = when (entityType) {
            FROM -> {
                legFrom = value
                if (VECTOR.equals(rowType)) {
                    rightPad(PLACEHOLDER, 12) + rightPad(PLACEHOLDER, 22)
                } else rightPad(ensureNotEmpty(value), 12)
            }
            TO -> rightPad(ensureNotEmpty(value), 22)
            else -> ""
        }
        body.append(entry)
    }

    override fun setValue(entityType: Entities?, aValue: Float?) {
        val entry = when (entityType) {
            DISTANCE -> leftPad(ensureNotEmpty(distanceInMeters(aValue), "0.00"), 8)
            COMPASS, INCLINATION -> leftPad(ensureNotEmpty(aValue, "0.00"), 8)
            LEFT, RIGHT, UP, DOWN -> leftPad(ensureNotEmpty(distanceInMeters(aValue)), 7)
            else -> ""
        }
        body.append(entry)
    }

    override fun setPhoto(aPhoto: Photo?) {
        // TODO
    }

    override fun setLocation(aLocation: Location) {
        // use the first location
        if (COORDINATE_PLACEHOLDER.equals(location)) {
            val utmCoordinate = UtmCoordinate(aLocation.latitude, aLocation.longitude);
            location = SEPARATOR + utmCoordinate.easting / 1000 +
                SEPARATOR + utmCoordinate.northing / 1000  +
                SEPARATOR + aLocation.altitude + ".00" +
                SEPARATOR + "UTM" + utmCoordinate.zone
            entrance = "Entree " + legFrom
        }
    }

    override fun getContent(): InputStream {
        // apply the location
        val troContents = body.toString()
                .replace(COORDINATE_PLACEHOLDER, location)
                .replace(ENTRANCE, entrance)
        // result
        return ByteArrayInputStream(troContents.toByteArray());
    }

    override fun prepare(aProject: Project) {
        Log.i(Constants.LOG_TAG_SERVICE, "Start Visual Topo export ")
        body.clear()

        // headers
        body.appendln("Version 5.11")
        body.appendln("Verification 1")
        body.appendln()
        body.append("Trou ")
            .append(aProject.name)
            .appendln(COORDINATE_PLACEHOLDER)
        body.appendln("Entree A0")
        body.appendln("Couleur 0,0,0")
        body.appendln()
        body.append("Param Deca ")
            .append(if (UNIT_GRADS.equals(getOptionValue(CODE_AZIMUTH_UNITS))) "Gra" else "Degd")
            .append(" Clino ")
            .append(if (UNIT_GRADS.equals(getOptionValue(CODE_SLOPE_UNITS))) "Gra" else "Degd")
            .append(" 0.0000 Dir,Dir,Dir Inc Std ")
            .append(formatDate(aProject.creationDate))
            .append(" A ;Generated by CaveSurvey ")
            .append(AndroidUtil.getAppVersion())
        body.appendln(";")
        body.appendln()
        body.appendln("A0          A0                        0.00    0.00    0.00      *      *      *      * N I * *")

        Log.i(Constants.LOG_TAG_SERVICE, "Generated body: $body")
    }

    override fun prepareEntity(rowCounter: Int, type: ExportEntityType) {
        rowType = type
    }

    override fun endEntity(rowCounter: Int) {

        if (VECTOR.equals(rowType)) {
            // l/r/u/d not sent for vectors
            body.append(leftPad(PLACEHOLDER, 7))
            body.append(leftPad(PLACEHOLDER, 7))
            body.append(leftPad(PLACEHOLDER, 7))
            body.append(leftPad(PLACEHOLDER, 7))
        }

        //First flag : orientation of the shot and the followings shots in extended elevation, N for normal, I for reverse
        //Second flag : Exclusion of this shot from development, I for include, E for exclude. Splay shots must always be excluded.
        //Third flag : D if a splay is a detail, not the wall, M for the wall.
        //Fourth flag : S if you want a vertical section (only if there are enough splay shots to compute it), * otherwise
        body.append(" N ")
            .append(if (LEG.equals(rowType) || MIDDLE.equals(rowType)) "I" else "E")
            .append(if (LEG.equals(rowType) || MIDDLE.equals(rowType)) " *" else " M")
            .appendln(" *")
    }

    override fun setDrawing(aSketch: Sketch?) {
        // TODO
    }

    private fun format(value: Float): String {
        return "%.2f".format(value)
    }

    private fun leftPad(value: String, length: Int): String {
        return value.padStart(length, ' ')
    }

    private fun rightPad(value: String, length: Int): String {
        return value.padEnd(length, ' ')
    }

    private fun ensureNotEmpty(value: String?): String {
        return ensureNotEmpty(value, PLACEHOLDER)
    }

    private fun ensureNotEmpty(value: String?, placeholder: String): String {
        return value ?: placeholder
    }

    private fun ensureNotEmpty(value: Float?): String {
        return ensureNotEmpty(value, PLACEHOLDER)
    }

    private fun distanceInMeters(value: Float?): Float? {
        // Visual Topo supports only meters
        return if (distanceInMeters) value else MapUtilities.getFeetsInMeters(value);
    }

    private fun ensureNotEmpty(value: Float?, placeholder: String): String {
        if (value == null) {
            return placeholder
        }
        return format(value)
    }

    companion object {
        val HEADER_DATE_FORMAT = "dd/MM/yyyy"

        fun formatDate(date: Date): String {
            return SimpleDateFormat(HEADER_DATE_FORMAT).format(date)
        }
    }
}