package qupath.ext.py4j.core;

import qupath.lib.gui.scripting.QPEx;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.imagej.images.servers.ImageJServer;
import qupath.lib.images.servers.bioformats.BioFormatsImageServer;
import qupath.lib.images.servers.openslide.OpenslideImageServer;
import qupath.lib.images.servers.ImageServerBuilder;
import com.google.gson.Gson;

import qupath.fx.utils.FXUtils;
import qupath.lib.gui.panes.ImageDetailsPane;
import qupath.lib.gui.prefs.PathPrefs;
import qupath.lib.gui.tools.GuiTools;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.images.servers.ImageServers;
import qupath.lib.images.servers.openslide.OpenslideServerBuilder;
import qupath.lib.images.servers.bioformats.BioFormatsServerBuilder;
import qupath.imagej.images.servers.ImageJServerBuilder;
import qupath.lib.io.GsonTools;
import qupath.lib.projects.ProjectImageEntry;
import qupath.lib.projects.Project;
import qupath.lib.gui.commands.Commands;

import qupath.lib.projects.Projects;
import qupath.lib.projects.ProjectIO;
import qupath.lib.gui.commands.ProjectCommands;
import qupath.lib.regions.RegionRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.io.FileWriter;
import java.net.URISyntaxException;


/**
 * Add a few GUI methods on top of {@link QuPathEntryPoint}.
 */
public class QuPathFX extends QuPathEntryPoint {

	/**
	 * Refresh the current project {@link QPEx#getProject() getProject()} in QuPath.
	 * This should be called whenever the current project has changed
	 * (e.g. by adding or removing items)
	 *
	 * @see QPEx#getProject()
	 * @see QuPathGUI#refreshProject()
	 */
	public static void refreshProjectInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			getQuPath().refreshProject();
			return null;
		});
	}

	/**
	 * Repaint the entire image in the current viewer
	 * {@link QPEx#getCurrentViewer() getCurrentViewer()} in QuPath.
	 * This should be called whenever a major change in display is triggered,
	 * such as changing the brightness/contrast or lookup table.
	 *
	 * @see QPEx#getCurrentViewer()
	 * @see QuPathViewer#repaintEntireImage()
	 */
	public static void repaintEntireImageInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			getCurrentViewer().repaintEntireImage();
			return null;
		});
	}

	/**
	 * Open project <code>project</code> in QuPath.
	 * It will become the current project
	 * and can be retrieved by {@link QPEx#getProject() getProject()}
	 *
	 * @param project the project to open
	 *
	 * @see QPEx#getProject()
	 * @see QuPathGUI#setProject(Project)
	 */
	public static void openProjectInQuPath(Project<BufferedImage> project) {
		FXUtils.callOnApplicationThread(() -> {
			getQuPath().setProject(project);
			return null;
		});
	}

	/**
	 * Close the current project {@link QPEx#getProject() getProject()} in QuPath.
	 * {@link QPEx#getProject() getProject()} will return <b>null</b> when this is done.
	 *
	 * @see QPEx#getProject()
	 * @see Commands#closeProject(QuPathGUI)
	 */
	public static void closeProjectInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			Commands.closeProject(getQuPath());
			return null;
		});
	}

	/**
	 * Open image <code>imageData</code> in the current viewer
	 * {@link QPEx#getCurrentViewer() getCurrentViewer()} in QuPath.
	 * It will become the current image data and
	 * can be retrieved by {@link QPEx#getCurrentImageData() getCurrentImageData()}.
	 *
	 * <p>
	 *     If the current project contains <code>imageData</code>,
	 *     the associated {@link ProjectImageEntry} will become the current project entry
	 *     and can be retrieved by {@link QPEx#getProjectEntry() getProjectEntry()}.
	 *     Otherwise, the current project will be closed.
	 * </p>
	 *
	 * @param imageData the image to open
	 *
	 * @see QPEx#getCurrentImageData()
	 * @see QPEx#getProjectEntry()
	 * @see QuPathViewer#setImageData(ImageData)
	 */
	public static void openImageDataInQuPath(ImageData<BufferedImage> imageData) {
		FXUtils.callOnApplicationThread(() -> {
//			saveCurrentImageData();
			if ((getProject() != null) && (getProject().getEntry(imageData) == null)) {
				closeProjectInQuPath();
			}
			getCurrentViewer().setImageData(imageData);
			getQuPath().refreshProject();
			return null;
		});
	}

	/**
	 * Close the current image data {@link QPEx#getCurrentImageData() getCurrentImageData()}
	 * in QuPath. {@link QPEx#getCurrentImageData() getCurrentImageData()} will return <b>null</b>
	 * when this is done.
	 *
	 * <p>
	 *     The associated project entry will become "inactive". That is,
	 *     {@link QPEx#getProjectEntry() getProjectEntry()} will return <b>null</b>.
	 * </p>
	 *
	 * @see QPEx#getCurrentImageData()
	 * @see QPEx#getProjectEntry()
	 * @see QuPathViewer#resetImageData()
	 */
	public static void closeImageDataInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
//			saveCurrentImageData();
			getCurrentViewer().resetImageData();
			getQuPath().refreshProject();
			return null;
		});
	}
	
//
//	 comment out these 3 methods
//	 1. use OpenImageDataInPath(imageData) to open the entry
//	 2. with imageData = entry.readImageData() <- this is how to get the imageData with GUI!
//	 3. this will ensure the same codes (GUI or not) -
//	    a. imageData == getCurrentImageData()
//	    b. imageData is the associated imageData of entry
//	 4. entry.saveImageData(imageData) to save your analysis results
//

//	/**
//	 * Open an image entry in QuPath.
//	 * <p>
//	 * If the current image data has been changed, it will be saved before opening
//	 * the new image entry.
//	 * </p>
//	 *
//	 * @param entry the image entry to open
//	 * @return true if the image entry was opened, false otherwise
//	 */
//	public static boolean openImageEntryInQuPath(ProjectImageEntry<BufferedImage> entry) {
//		return FXUtils.callOnApplicationThread(() -> {
//			saveCurrentImageData();
//			return getQuPath().openImageEntry(entry);
//		});
//	}

//	/**
//	 * Close the current image entry in QuPath.
//	 * <p>
//	 * If the current image data has been changed, it will be saved before closing
//	 * the image entry.
//	 * </p>
//	 */
//	public static void closeImageEntryInQuPath() {
//		FXUtils.callOnApplicationThread(() -> {
//			saveCurrentImageData();
//			getCurrentViewer().resetImageData();
//			refreshProject();
//			return null;
//		});
//	}

//	/**
//	 * Close the specified image entry in QuPath.
//	 * <p>
//	 * If the current image data has been changed, it will be saved before closing
//	 * the image entry.
//	 * </p>
//	 *
//	 * @param entry the image entry to close
//	 */
//	public static void closeImageEntryInQuPath(ProjectImageEntry<BufferedImage> entry) {
//		FXUtils.callOnApplicationThread(() -> {
//			if (entry == getProject().getEntry(getCurrentImageData())) {
//				closeImageEntryInQuPath();
//			}
//			return null;
//		});
//	}
}
