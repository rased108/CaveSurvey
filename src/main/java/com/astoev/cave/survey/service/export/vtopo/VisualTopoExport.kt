package com.astoev.cave.survey.service.export.vtopo

import android.content.Context
import android.util.Log
import com.astoev.cave.survey.Constants
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
    val PLACEHOLDER = "*"

    private var body = StringBuilder()
    private var rowType: ExportEntityType? = null
    private var location: String = COORDINATE_PLACEHOLDER

    init {
        mUseUniqueName = true;
        mExtension = ".tro";
        body.clear()
    }

    override fun setValue(entityType: Entities, value: String) {
        val entry = when (entityType) {
            FROM -> if (VECTOR.equals(rowType)) {
                        rightPad(PLACEHOLDER, 12) + rightPad(PLACEHOLDER, 22)
                    } else rightPad(ensureNotEmpty(value), 12)
            TO -> rightPad(ensureNotEmpty(value), 22)
            else -> ""
        }
        body.append(entry)
    }

    override fun setValue(entityType: Entities?, aValue: Float?) {
        val entry = when (entityType) {
            DISTANCE, COMPASS, INCLINATION -> leftPad(ensureNotEmpty(aValue), 8)
            LEFT, RIGHT, UP, DOWN -> leftPad(ensureNotEmpty(aValue), 7)
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
            location = SEPARATOR + utmCoordinate.easting +
                SEPARATOR + utmCoordinate.northing  +
                SEPARATOR + aLocation.altitude + ".00" +
                SEPARATOR + "UTM"
        }
    }

    override fun getContent(): InputStream {
        // apply the location
        val troContents = body.toString().replace(COORDINATE_PLACEHOLDER, location);
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
        body.append(aProject.name)
            .appendln(COORDINATE_PLACEHOLDER)
        body.appendln("Entree A0")
        body.appendln("Couleur 255,255,255")
        body.appendln()
        body.append("Param Deca ")
            .append(if (UNIT_GRADS.equals(getOptionValue(CODE_AZIMUTH_UNITS))) "Gra" else "Degd")
            .append(" Clino ")
            .append(if (UNIT_GRADS.equals(getOptionValue(CODE_SLOPE_UNITS))) "Gra" else "Degd")
            .append(" 0.0000 Dir,Dir,Dir Inc Std ")
            .append(formatDate(aProject.creationDate))
            .append(" M ;Generated by CaveSurvey ")
            .append(AndroidUtil.getAppVersion())
        body.appendln(";")
        body.appendln()

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
            .appendln(" M S")
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
        return value ?: PLACEHOLDER
    }

    private fun ensureNotEmpty(value: Float?): String {
        if (value == null) {
            return PLACEHOLDER
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