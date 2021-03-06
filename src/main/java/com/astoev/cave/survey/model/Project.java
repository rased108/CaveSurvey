package com.astoev.cave.survey.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: astoev
 * Date: 1/24/12
 * Time: 11:14 PM
 * To change this template use File | Settings | File Templates.
 */
@DatabaseTable(tableName = "projects")
public class Project implements Serializable, Comparable<Project> {

    public static final String COLUMN_NAME = "name";

    @DatabaseField(generatedId = true, canBeNull = false, columnName = "id")
    private Integer mId;
    @DatabaseField(columnName = COLUMN_NAME)
    private String mName;
    @ForeignCollectionField()
    private ForeignCollection<Leg> mLegs;
    @DatabaseField(columnName = "creation_date", dataType = DataType.DATE)
    private Date mCreationDate;


    public Project() {

    }


    public Integer getId() {
        return mId;
    }

    public void setId(Integer aId) {
        mId = aId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

    public ForeignCollection<Leg> getLegs() {
        return mLegs;
    }

    public void setLegs(ForeignCollection<Leg> aLegs) {
        mLegs = aLegs;
    }

    public Date getCreationDate() {
        return mCreationDate;
    }

    public void setCreationDate(Date aCreationDate) {
        mCreationDate = aCreationDate;
    }

    /**
     * Returns only the name of the project as we use it inside ArrayAdapter for ListView 
     */
	@Override
	public String toString() {
		return getName();
	}

    @Override
    public int compareTo(Project o) {
        return o.getCreationDate().compareTo(getCreationDate());
    }
}
