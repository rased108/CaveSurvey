/**
 *
 */
package com.astoev.cave.survey.util;

import android.util.Log;

import com.astoev.cave.survey.Constants;
import com.astoev.cave.survey.model.Gallery;
import com.astoev.cave.survey.model.Leg;
import com.astoev.cave.survey.model.Location;
import com.astoev.cave.survey.model.Note;
import com.astoev.cave.survey.model.Photo;
import com.astoev.cave.survey.model.Point;
import com.astoev.cave.survey.model.Project;
import com.astoev.cave.survey.model.Sketch;
import com.astoev.cave.survey.model.Vector;
import com.astoev.cave.survey.service.Workspace;
import com.astoev.cave.survey.service.ormlite.DatabaseHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author jmitrev
 */
public class DaoUtil {

    public static Note getActiveLegNote(Leg aActiveLeg) throws SQLException {
        QueryBuilder<Note, Integer> query = Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryBuilder();
        query.where().eq(Note.COLUMN_POINT_ID, aActiveLeg.getFromPoint().getId());
        return Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryForFirst(query.prepare());
    }
    
    public static Note getNoteByPoint(Point pointArg) throws SQLException{
        QueryBuilder<Note, Integer> query = Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryBuilder();
        query.where().eq(Note.COLUMN_POINT_ID, pointArg.getId());
        return Workspace.getCurrentInstance().getDBHelper().getNoteDao().queryForFirst(query.prepare());
    }

    public static Sketch getScetchByLeg(Leg legArg) throws SQLException {
        return getScetchByPoint(legArg.getFromPoint());
    }
    
    public static Photo getPhotoByLeg(Leg legALeg) throws SQLException{
    	return getPhotoByPoint(legALeg.getFromPoint());
    }

    public static Sketch getScetchByPoint(Point pointArg) throws SQLException {
        QueryBuilder<Sketch, Integer> query = Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryBuilder();
        query.where().eq(Sketch.COLUMN_POINT_ID, pointArg.getId());
        return Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryForFirst(query.prepare());
    }
    
    public static List<Sketch> getAllScetchesByPoint(Point pointArg) throws SQLException {
        QueryBuilder<Sketch, Integer> query = Workspace.getCurrentInstance().getDBHelper().getSketchDao().queryBuilder();
        query.where().eq(Sketch.COLUMN_POINT_ID, pointArg.getId());
        return Workspace.getCurrentInstance().getDBHelper().getSketchDao().query(query.prepare());
    }

    
    public static Photo getPhotoByPoint(Point pointArg) throws SQLException {
    	QueryBuilder<Photo, Integer> query = Workspace.getCurrentInstance().getDBHelper().getPhotoDao().queryBuilder();
    	query.where().eq(Photo.COLUMN_POINT_ID, pointArg.getId());
    	return Workspace.getCurrentInstance().getDBHelper().getPhotoDao().queryForFirst(query.prepare());
    }
    
    public static List<Photo> getAllPhotosByPoint(Point pointArg) throws SQLException {
        QueryBuilder<Photo, Integer> query = Workspace.getCurrentInstance().getDBHelper().getPhotoDao().queryBuilder();
        query.where().eq(Photo.COLUMN_POINT_ID, pointArg.getId());
        return Workspace.getCurrentInstance().getDBHelper().getPhotoDao().query(query.prepare());
    }
    
    public static Location getLocationByPoint(Point pointArg) throws SQLException {
        Dao<Location, Integer> locationDao = Workspace.getCurrentInstance().getDBHelper().getLocationDao();
        QueryBuilder<Location, Integer> query = locationDao.queryBuilder();
        query.where().eq(Location.COLUMN_POINT_ID, pointArg.getId());
        return locationDao.queryForFirst(query.prepare());
    }

    public static Project getProject(int aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getProjectDao().queryForId(aId);
    }

    public static Leg getLeg(int aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getLegDao().queryForId(aId);
    }


    public static Point getPoint(Integer aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getPointDao().queryForId(aId);
    }

    public static Gallery getGallery(Integer aId) throws SQLException {
        return Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryForId(aId);
    }

    public static List<Leg> getCurrProjectLegs() throws SQLException {
        return getProjectLegs(Workspace.getCurrentInstance().getActiveProjectId());
    }

    public static List<Leg> getProjectLegs(Integer aProjectId) throws SQLException {
        QueryBuilder<Leg, Integer> statementBuilder = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        statementBuilder.where().eq(Leg.COLUMN_PROJECT_ID, aProjectId);
        statementBuilder.orderBy(Leg.COLUMN_GALLERY_ID, true);
        statementBuilder.orderBy(Leg.COLUMN_FROM_POINT, true);
        statementBuilder.orderBy(Leg.COLUMN_TO_POINT, true);
		statementBuilder.orderBy(Leg.COLUMN_MIDDLE_POINT_AT_DISTANCE, true);

        return Workspace.getCurrentInstance().getDBHelper().getLegDao().query(statementBuilder.prepare());
    }

    public static void refreshPoint(Point aPoint) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getPointDao().refresh(aPoint);
    }

    public static Gallery createGallery(boolean isFirst) throws SQLException {
        Gallery gallery = new Gallery();
        Project currProject = Workspace.getCurrentInstance().getActiveProject();
        if (isFirst) {
            gallery.setName(Gallery.getFirstGalleryName());
        } else {
            gallery.setName(Gallery.generateNextGalleryName(currProject.getId()));
        }
        gallery.setProject(currProject);
        Workspace.getCurrentInstance().getDBHelper().getGalleryDao().create(gallery);
        return gallery;
    }

    public static Gallery getLastGallery(Integer aProjectId) throws SQLException {
        QueryBuilder<Gallery, Integer> query = Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryBuilder();
        query.where().eq(Gallery.COLUMN_PROJECT_ID, aProjectId);
        query.orderBy(Gallery.COLUMN_ID, false);
        return query.queryForFirst();
    }

    public static Leg getLegByToPoint(Point aToPoint) throws SQLException {
        // TODO this will work as soon as we keep a tree of legs. Once we start closing circles will break
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        query.where().eq(Leg.COLUMN_TO_POINT, aToPoint.getId());
        return query.queryForFirst();
    }

    public static boolean hasLegsByFromPoint(Point aFromPoint) throws SQLException {
        QueryBuilder<Leg, Integer> query = Workspace.getCurrentInstance().getDBHelper().getLegDao().queryBuilder();
        query.where().eq(Leg.COLUMN_FROM_POINT, aFromPoint.getId());
        return query.countOf() > 0;
    }

    public static long getGalleriesCount(Integer aActiveProjectId) throws SQLException {
        QueryBuilder<Gallery, Integer> query = Workspace.getCurrentInstance().getDBHelper().getGalleryDao().queryBuilder();
        query.where().eq(Gallery.COLUMN_PROJECT_ID, aActiveProjectId);
        return query.countOf();
    }
    
    /**
     * DAO method that saves or update the location of Point based on GPS location 
     * 
     * @param parentPointArg - parent Point
     * @param gpsLocationArg - GPS Location
     * @throws SQLException if there is a problem working with the DB
     */
    public static void saveLocationToPoint(final Point parentPointArg, final android.location.Location gpsLocationArg)
        throws SQLException {
        
        ConnectionSource connetionSource = Workspace.getCurrentInstance().getDBHelper().getConnectionSource();
        TransactionManager.callInTransaction(connetionSource, new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                Location oldLocation = getLocationByPoint(parentPointArg);
                if (oldLocation != null){
                    oldLocation.setLatitude(gpsLocationArg.getLatitude());
                    oldLocation.setLongitude(gpsLocationArg.getLongitude());
                    oldLocation.setAltitude((int)gpsLocationArg.getAltitude());
                    oldLocation.setAccuracy((int)gpsLocationArg.getAccuracy());
                    Workspace.getCurrentInstance().getDBHelper().getLocationDao().update(oldLocation);
                    
                    Log.i(Constants.LOG_TAG_DB, "Update location with id:" + oldLocation.getId() + " for point:" + parentPointArg.getId());
                    return oldLocation.getId();
                } else {
                    Location newLocation = new Location();
                    newLocation.setPoint(parentPointArg);
                    newLocation.setLatitude(gpsLocationArg.getLatitude());
                    newLocation.setLongitude(gpsLocationArg.getLongitude());
                    newLocation.setAltitude((int)gpsLocationArg.getAltitude());
                    newLocation.setAccuracy((int)gpsLocationArg.getAccuracy());
                    Workspace.getCurrentInstance().getDBHelper().getLocationDao().create(newLocation);
                    
                    Log.i(Constants.LOG_TAG_DB, "Creted location with id:" + newLocation.getId() + " for point:" + parentPointArg.getId());
                    return newLocation.getId();
                }
            }
        });
    }
    
    /**
     * Deletes a leg its toPoint and all the data that is related to toPoint
     * 
     * @param legArg - leg to delete
     * @return true if the leg is successfully deleted
     * @throws SQLException if there is a problem with DB
     */
    public static boolean deleteLeg(final Leg legArg) throws SQLException{
        if (legArg.isNew()){
            return false;
        }
        
        final Workspace workspace = Workspace.getCurrentInstance();
        final DatabaseHelper dbHelper = Workspace.getCurrentInstance().getDBHelper();
        
        TransactionManager.callInTransaction(dbHelper.getConnectionSource(), new Callable<Object>() {
            public Object call() throws Exception {
                Log.d(Constants.LOG_TAG_DB, "Deleting " + workspace.getActiveLegId());

                if (legArg.isMiddle()) {

                    // delete middle leg
                    int deletedLeg = dbHelper.getLegDao().delete(legArg);
                    Log.d(Constants.LOG_TAG_DB, "Deleted middle leg:" + deletedLeg);

                    workspace.setActiveLeg(DaoUtil.getLegByToPoint(legArg.getToPoint()));
                } else {

                    Point toPoint = legArg.getToPoint();

                    // delete note
                    Note note = DaoUtil.getNoteByPoint(toPoint);
                    if (note != null) {
                        int deleted = dbHelper.getNoteDao().delete(note);
                        Log.d(Constants.LOG_TAG_DB, "Deleted note:" + deleted);
                    }

                    // delete location
                    Location location = DaoUtil.getLocationByPoint(toPoint);
                    if (location != null) {
                        int deleted = dbHelper.getLocationDao().delete(location);
                        Log.d(Constants.LOG_TAG_DB, "Deleted location:" + deleted);
                    }

                    // delete photos
                    List<Photo> photosList = DaoUtil.getAllPhotosByPoint(toPoint);
                    if (photosList != null && !photosList.isEmpty()) {
                        int deleted = dbHelper.getPhotoDao().delete(photosList);
                        Log.d(Constants.LOG_TAG_DB, "Deleted photos:" + deleted);
                    }

                    // delete sketches
                    List<Sketch> sketchList = DaoUtil.getAllScetchesByPoint(toPoint);
                    if (sketchList != null && !sketchList.isEmpty()) {
                        int deleted = dbHelper.getSketchDao().delete(sketchList);
                        Log.d(Constants.LOG_TAG_DB, "Deleted sketches:" + deleted);
                    }

                    // TODO delete middle points
                    // TODO delete vectors

                    // delete leg
                    int deletedLeg = dbHelper.getLegDao().delete(legArg);
                    Log.d(Constants.LOG_TAG_DB, "Deleted leg:" + deletedLeg);

                    // delete to point
                    int deletedPoint = dbHelper.getPointDao().delete(toPoint);
                    Log.d(Constants.LOG_TAG_DB, "Deleted point:" + deletedPoint);

                    workspace.setActiveLeg(workspace.getLastLeg());
                }

                return null;
            }
        });        
        return true;
    }
    
    /**
     * Creates an ProjectInfo that sums up the project
     * 
     * @return ProjectInfo
     * @throws SQLException if there is an DB problem
     */
    public static ProjectInfo getProjectInfo() throws SQLException{
        
        List<Leg> legs = DaoUtil.getCurrProjectLegs();
        Project project = Workspace.getCurrentInstance().getActiveProject();
        String name = project.getName();
        String creationDate = project.getCreationDateFormatted();
        
        float totalLength = 0, totalDepth = 0;
        int numNotes = 0, numDrawings = 0, numCoordinates = 0, numPhotos = 0;
        for (Leg l : legs) {
            
            // TODO calculate the correct distance and depth
            if (!l.isMiddle() && l.getDistance() != null) {
                totalLength += l.getDistance();
            }

            // notes
            if (DaoUtil.getActiveLegNote(l) != null) {
                numNotes ++;
            }

            Point fromPoint = l.getFromPoint();
            
            // drawings
            List<Sketch> sketchesList = DaoUtil.getAllScetchesByPoint(fromPoint);
            if (sketchesList != null && !sketchesList.isEmpty()){
                numDrawings += sketchesList.size();
            }

            // gps
            Location locaiton = DaoUtil.getLocationByPoint(fromPoint);
            if (locaiton != null ) {
                numCoordinates ++;
            }

            // photos
            List<Photo>  photos = DaoUtil.getAllPhotosByPoint(fromPoint);
            if (photos != null && !photos.isEmpty()) {
                numPhotos += photos.size();
            }
        }
        
        int numGalleries = (int)DaoUtil.getGalleriesCount(project.getId());
        
        ProjectInfo projectInfo = new ProjectInfo(name, creationDate, numGalleries, legs.size(), totalLength, totalDepth);
        
        projectInfo.setNotes(numNotes);
        projectInfo.setSketches(numDrawings);
        projectInfo.setLocations(numCoordinates);
        projectInfo.setPhotos(numPhotos);
        
        return projectInfo;
    }

    public static List<Vector> getLegVectors(Leg aLegEdited) throws SQLException {
        QueryBuilder<Vector, Integer> vectorsQuery = Workspace.getCurrentInstance().getDBHelper().getVectorsDao().queryBuilder();
        vectorsQuery.where().eq(Vector.COLUMN_POINT, aLegEdited.getFromPoint().getId());
        vectorsQuery.orderBy(Vector.COLUMN_ID, true);
        return vectorsQuery.query();
    }

    public static boolean hasVectorsByPoint(Point aFromPoint) throws SQLException {
        QueryBuilder<Vector, Integer> vectorsQuery = Workspace.getCurrentInstance().getDBHelper().getVectorsDao().queryBuilder();
        vectorsQuery.where().eq(Vector.COLUMN_POINT, aFromPoint.getId());
        return vectorsQuery.countOf() > 0;
    }

    public static void saveVector(Vector aVector) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getVectorsDao().create(aVector);
    }

    public static void deleteVector(Vector aVector) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getVectorsDao().delete(aVector);
    }

    public static boolean deleteProject(final Integer aProjectId) {

        Log.i(Constants.LOG_TAG_DB, "Delete project " + aProjectId);


        try {
            TransactionManager.callInTransaction(Workspace.getCurrentInstance().getDBHelper().getConnectionSource(), new Callable<Object>() {
                public Object call() throws Exception {
                    List<Leg> legs = getProjectLegs(aProjectId);
                    if (legs != null) {
                        for (Leg l: legs) {
                            // delete photos
                            List<Photo> photos = getAllPhotosByPoint(l.getFromPoint());
                            if (photos != null  && photos.size() > 0) {
                                for (Photo p: photos) {
                                    FileUtils.deleteQuietly(new File(p.getFSPath()));
                                    Workspace.getCurrentInstance().getDBHelper().getPhotoDao().delete(p);
                                }
                            }

                            // delete sketches
                            List<Sketch> sketches = getAllScetchesByPoint(l.getFromPoint());
                            if (sketches != null && sketches.size() > 0) {
                                for (Sketch s: sketches) {
                                    FileUtils.deleteQuietly(new File(s.getFSPath()));
                                    Workspace.getCurrentInstance().getDBHelper().getSketchDao().delete(s);
                                }
                            }

                            // delete vectors
                            List<Vector> vectors = getLegVectors(l);
                            if (vectors != null && vectors.size() > 0) {
                                Workspace.getCurrentInstance().getDBHelper().getVectorsDao().delete(vectors);
                            }

                            // delete locations
                            Location location = getLocationByPoint(l.getFromPoint());
                            if (location != null) {
                                Workspace.getCurrentInstance().getDBHelper().getLocationDao().delete(location);
                            }

                            // delete points
                            Workspace.getCurrentInstance().getDBHelper().getPointDao().delete(l.getFromPoint());
                            Workspace.getCurrentInstance().getDBHelper().getPointDao().delete(l.getToPoint());

                            // delete leg
                            Workspace.getCurrentInstance().getDBHelper().getLegDao().delete(l);
                        }
                    }

                    // delete galleries
                    Gallery g;
                    while ((g = DaoUtil.getLastGallery(aProjectId)) != null) {
                        DaoUtil.deleteGallery(g);
                    }

                    // delete project
                    Project p = getProject(aProjectId);
                    Workspace.getCurrentInstance().getDBHelper().getProjectDao().delete(p);

                    FileUtils.deleteQuietly(FileStorageUtil.getProjectHome(p.getName()));

                    Log.i(Constants.LOG_TAG_DB, "Deleted project " + aProjectId);

                    return null;
                }
            });
        } catch (SQLException e) {
            Log.e(Constants.LOG_TAG_DB, "Failed to delete project", e);
            return false;
        }

        return true;
    }

    private static void deleteGallery(Gallery aG) throws SQLException {
        Workspace.getCurrentInstance().getDBHelper().getGalleryDao().delete(aG);
    }
}
