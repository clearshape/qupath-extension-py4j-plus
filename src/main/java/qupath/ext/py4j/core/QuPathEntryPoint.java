package qupath.ext.py4j.core;

import qupath.fx.utils.FXUtils;
import qupath.lib.gui.panes.ImageDetailsPane;
import qupath.lib.gui.prefs.PathPrefs;
import qupath.lib.gui.tools.GuiTools;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.io.GsonTools;
import qupath.lib.projects.ProjectImageEntry;
import qupath.lib.projects.Project;
import qupath.lib.gui.commands.Commands;
import qupath.lib.images.servers.ImageServers;
import qupath.lib.projects.Projects;
import qupath.lib.projects.ProjectIO;
import qupath.lib.gui.commands.ProjectCommands;
import qupath.lib.regions.RegionRequest;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.util.Base64;

/**
 * Entry point for use with a Py4J Gateway.
 * This provides useful methods to work with QuPath from Python.
 */
public class QuPathEntryPoint extends QuPathEntryPointBase {

	/**
	 * Refresh the current project in QuPath.
	 */
	public static void refreshProjectInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			getQuPath().refreshProject();
			return null;
		});
	}

	/**
	 * Refresh the current project in QuPath.
	 */
	public static void refreshProject() {
		refreshProjectInQuPath();
	}

	/**
	 * Repaint the entire image in the current Viewer.
	 */
	public static void repaintEntireImageInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			getCurrentViewer().repaintEntireImage();
			return null;
		});
	}

	/**
	 * Repaint the entire image in the current Viewer.
	 */
	public static void repaintEntireImage() {
		repaintEntireImageInQuPath();
	}

	/**
	 * Open a project in QuPath.
	 *
	 * @param project the project to open
	 */
	public static void openProjectInQuPath(Project<BufferedImage> project) {
		FXUtils.callOnApplicationThread(() -> {
			getQuPath().setProject(project);
			return null;
		});
	}

	/**
	 * Close the current project in QuPath.
	 *
	 * @return the closed project
	 */
	public static void closeProjectInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			Commands.closeProject(getQuPath());
			return null;
		});
	}

	/**
	 * Open image data in QuPath.
	 * <p>
	 * If the current image data has been changed, it will be saved before opening
	 * the new image data.
	 * </p>
	 *
	 * @param imageData the image data to open
	 */
	public static void openImageDataInQuPath(ImageData<BufferedImage> imageData) {
		FXUtils.callOnApplicationThread(() -> {
			saveCurrentImageData();
			getCurrentViewer().setImageData(imageData);
			return null;
		});
	}

	/**
	 * Close the current image data in QuPath.
	 * <p>
	 * If the current image data has been changed, it will be saved before closing it.
	 * </p>
	 *
	 * @return the closed image data
	 */
	public static void closeImageDataInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			saveCurrentImageData();
			getCurrentViewer().resetImageData();
	//		refreshProject();
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

	/**
	 * Create a new project.
	 *
	 * @param projectPath the path to the project
	 * @return the created project
	 */
	public static Project<BufferedImage> createProject(String projectPath) {
		mkdirs(projectPath);
		return Projects.createProject(new File(projectPath), BufferedImage.class);
	}

	/**
	 * Save a project.
	 *
	 * @param project the project to save
	 * @throws IOException if an error occurs while saving the project
	 */
	public static void saveProject(Project<BufferedImage> project) throws IOException {
		if (project != null) {
			project.syncChanges();
		}
	}

	/**
	 * Load a project.
	 *
	 * @param projectPath the path to the project
	 * @return the loaded project
	 * @throws IOException if an error occurs while loading the project
	 */
	public static Project<BufferedImage> loadProject(String projectPath) throws IOException {
		File file = new File(projectPath);
		if (file.isDirectory()) {
			file = new File(file, ProjectIO.DEFAULT_PROJECT_NAME + ProjectIO.getProjectExtension(true));
		}
		return ProjectIO.loadProject(file, BufferedImage.class);
	}

	/**
	 * Add an image entry to a project, with a specified image type.
	 *
	 * @param project the project to add the image entry to
	 * @param server  the image server to add
	 * @param type    the image type, or null (hasImageData() == false)
	 * @return the added image entry
	 * <p>The thumbnail of the entry is refreshed.</p>
	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
	 *      ImageData.ImageType)
	 */
	public static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			ImageServer<BufferedImage> server,
			ImageData.ImageType type) throws IOException {
		var entry = ProjectCommands.addSingleImageToProject(project, server, type);
		refreshThumbnail(entry, server);	// refresh its thumbnail
		return entry;
	}

	/**
	 * Add an image entry to a project, with the estimated image type.
	 *
	 * @param project the project to add the image entry to
	 * @param server  the image server to add
	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
	 *      ImageData.ImageType)
	 */
	public static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			ImageServer<BufferedImage> server) throws IOException {
		return addImageEntry(project, server, estimatedImageType(server));
	}

	/**
	 * Add an image entry to a project, with a specified image type.
	 *
	 * @param project   the project to add the image entry to
	 * @param imagePath the image file to add
	 * @param type      the image type, or null (hasImageData() == false)
	 * @return the added image entry
	 * @throws IOException if an error occurs while loading the image file
	 */
	public static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			String imagePath,
			ImageData.ImageType type) throws IOException {
		return addImageEntry(project, ImageServers.buildServer(imagePath), type);
	}

	/**
	 * Add an image entry to a project, with the estimated image type.
	 *
	 * @param project   the project to add the image entry to
	 * @return the added image entry
	 * @throws IOException if an error occurs while loading the image file
	 */
	public static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			String imagePath) throws IOException {
		return addImageEntry(project, ImageServers.buildServer(imagePath));
	}

	/**
	 * Remove an image entry from a project.
	 *
	 * @param project       the project to remove the image entry from
	 * @param entry         the image entry to remove
	 * @param removeAllData whether to remove all data associated with the image
	 *                      entry
	 */
	public static void removeImageEntry(
			Project<BufferedImage> project,
			ProjectImageEntry<BufferedImage> entry,
			boolean removeAllData) {
		project.removeImage(entry, removeAllData);
	}

	/**
	 * Remove an image entry (including associated data) from a project.
	 *
	 * @param project the project to remove the image entry from
	 * @param entry   the image entry to remove
	 */
	public static void removeImageEntry(
			Project<BufferedImage> project,
			ProjectImageEntry<BufferedImage> entry) {
		project.removeImage(entry, true);
	}

	/**
	 * Create a new image data with the specified image type.
	 *
	 * @param server the image server to create the image data from
	 * @param type the image type
	 * @return the created image data
	 */
	public static ImageData<BufferedImage> createImageData(
			ImageServer<BufferedImage> server,
			ImageData.ImageType type) throws IOException {
		return new ImageData<BufferedImage>(server, type);
	}

	/**
	 * Create a new image data with the specified image type.
	 *
	 * @param imagePath the image file to create the image data from
	 * @param type the image type
	 * @return the created image data
	 */
	public static ImageData<BufferedImage> createImageData(
			String imagePath,
			ImageData.ImageType type) throws IOException {
		return createImageData(ImageServers.buildServer(imagePath), type);
	}

	/**
	 * Create a new image data with the estimated image type.
	 *
	 * @param server the image server to create the image data from
	 * @return the created image data
	 */
	public static ImageData<BufferedImage> createImageData(ImageServer<BufferedImage> server) throws IOException {
		return createImageData(server, estimatedImageType(server));
	}

	/**
	 * Create a new image data with the estimated image type.
	 *
	 * @param imagePath the image file to create the image data from
	 * @return the created image data
	 */
	public static ImageData<BufferedImage> createImageData(String imagePath) throws IOException {
		return createImageData(ImageServers.buildServer(imagePath));
	}

	/**
	 * Save the current image data if it has been changed.
	 *
	 * <p>
	 * If the current image data is not null and has been changed, it will be saved.
	 * </p>
	 * @throws IOException if an error occurs while saving the image data
	 */
	public static void saveCurrentImageData() throws IOException {
		var project = getProject();
		var imageData = getCurrentImageData();
		if ((project != null) && (imageData != null) && (imageData.isChanged())) {
			var entry = project.getEntry(imageData);
			if (entry != null) {
				entry.saveImageData(imageData);
			}
		}
	}

	/**
	 * Create a new image server.
	 *
	 * @param imagePath the path to the image
	 * @return the created image server
	 * @throws IOException if an error occurs while loading the image file
	 */
	public static ImageServer<BufferedImage> createImageServer(String imagePath) throws IOException {
		return ImageServers.buildServer(imagePath);
	}

	/**
	 * Save an image server to a JSON file.
	 *
	 * @param server         the image server to save
	 * @param jsonServerPath the path to save the image server to
	 * @throws IOException if an error occurs while saving the image server
	 */
	public static void saveImageServer(
			ImageServer<BufferedImage> server,
			String jsonServerPath) throws IOException {
		try (FileWriter writer = new FileWriter(jsonServerPath)) {
			String serverJson = GsonTools.getInstance().toJson(server);
			writer.write(serverJson);
		}
	}

	/**
	 * Load an image server from a JSON file.
	 *
	 * @param jsonServerPath the path to the JSON file
	 * @return the loaded image server
	 * @throws IOException if an error occurs while loading the image server
	 */
	public static ImageServer<BufferedImage> loadImageServer(String jsonServerPath) throws IOException {
		return ImageServers.buildServer(jsonServerPath);
	}

	/**
	 * Add an image entry to a project, with a specified image type.
	 *
	 * @param project the project to add the image entry to
	 * @param server  the image server to add
	 * @param server  the image server to add
	 * @param type    the image type
	 * @return the added image entry
	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
	 *      ImageData.ImageType)
	 */
	public static ProjectImageEntry<BufferedImage> addImage(
			Project<BufferedImage> project,
			ImageServer<BufferedImage> server,
			ImageData.ImageType type) throws IOException {
		return addImageEntry(project, server, type);
	}

	/**
	 * Add an image entry to a project, with an unknown image type.
	 *
	 * @param project the project to add the image entry to
	 * @param server  the image server to add
	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
	 *      ImageData.ImageType)
	 */
	public static ProjectImageEntry<BufferedImage> addImage(
			Project<BufferedImage> project,
			ImageServer<BufferedImage> server) throws IOException {
		return addImageEntry(project, server);
	}

	/**
	 * Add an image entry to a project, with a specified image type.
	 *
	 * @param project   the project to add the image entry to
	 * @param imagePath the image file to add
	 * @param type      the image type, or null to use the default
	 * @return the added image entry
	 * @throws IOException if an error occurs while loading the image file
	 */
	public static ProjectImageEntry<BufferedImage> addImage(
			Project<BufferedImage> project,
			String imagePath,
			ImageData.ImageType type) throws IOException {
		return addImageEntry(project, imagePath, type);
	}

	/**
	 * Add an image entry to a project, with an unknown image type.
	 *
	 * @param project   the project to add the image entry to
	 * @return the added image entry
	 * @throws IOException if an error occurs while loading the image file
	 */
	public static ProjectImageEntry<BufferedImage> addImage(
			Project<BufferedImage> project,
			String imagePath) throws IOException {
		return addImageEntry(project, imagePath);
	}

	/**
	 * Read the first z-slice and first time point of the provided image at the
	 * provided downsample and return an image with the default format 'imagej tiff'.
	 *
	 * @param server     the image to open
	 * @param downsample the downsample to use when reading the image
	 * @return an array of bytes described the requested image with the 'imagej
	 *         tiff' format
	 * @throws IOException when an error occurs while reading the image
	 */
	public static byte[] getImageBytes(
			ImageServer<BufferedImage> server,
			double downsample) throws IOException {
		return getImageBytes(server, downsample, "imagej tiff");
	}

	/**
	 * Read the first z-slice and first time point of a portion of the provided
	 * image at the provided downsample and return an image with the default format 'imagej tiff'.
	 *
	 * @param server     the image to open
	 * @param downsample the downsample to use when reading the image
	 * @param x          the x-coordinate of the portion of the image to retrieve
	 * @param y          the y-coordinate of the portion of the image to retrieve
	 * @param width      the width of the portion of the image to retrieve
	 * @param height     the height of the portion of the image to retrieve
	 * @return an array of bytes described the requested image with the 'imagej
	 *         tiff' format
	 * @throws IOException when an error occurs while reading the image
	 */
	public static byte[] getImageBytes(
			ImageServer<BufferedImage> server,
			double downsample,
			int x, int y, int width, int height) throws IOException {
		return getImageBytes(server, downsample, x, y, width, height, "imagej tiff");
	}

	/**
	 * Read a portion of the provided image at the provided downsample and return an
	 * image with the default format 'imagej tiff'.
	 *
	 * @param server     the image to open
	 * @param downsample the downsample to use when reading the image
	 * @param x          the x-coordinate of the portion of the image to retrieve
	 * @param y          the y-coordinate of the portion of the image to retrieve
	 * @param width      the width of the portion of the image to retrieve
	 * @param height     the height of the portion of the image to retrieve
	 * @param z          the z-slice of the image to retrieve
	 * @param t          the time point of the image to retrieve
	 * @return an array of bytes described the requested image with the 'imagej
	 *         tiff' format
	 * @throws IOException when an error occurs while reading the image
	 */
	public static byte[] getImageBytes(
			ImageServer<BufferedImage> server,
			double downsample,
			int x, int y, int width, int height,
			int z, int t) throws IOException {
		return getImageBytes(server, downsample, x, y, width, height, z, t, "imagej tiff");
	}

	/**
	 * Read a portion of the provided image and return an image with the default
	 * format 'imagej tiff'.
	 *
	 * @param server  the image to open
	 * @param request the region to read.
	 * @return an array of bytes described the requested image with the 'imagej
	 *         tiff' format
	 * @throws IOException when an error occurs while reading the image
	 */
	public static byte[] getImageBytes(
			ImageServer<BufferedImage> server,
			RegionRequest request) throws IOException {
		return getImageBytes(server, request, "imagej tiff");
	}

	/**
	 * Same as {@link #getImageBytes(ImageServer, double)}, but encoded with the
	 * {@link Base64} scheme.
	 */
	public static String getImageBase64(
			ImageServer<BufferedImage> server,
			double downsample) throws IOException {
		return getImageBase64(server, downsample, "imagej tiff");
	}

	/**
	 * Same as {@link #getImageBytes(ImageServer, double, int, int, int, int)}, but
	 * encoded with the {@link Base64} scheme.
	 */
	public static String getImageBase64(
			ImageServer<BufferedImage> server,
			double downsample,
			int x, int y, int width, int height) throws IOException {
		return getImageBase64(server, downsample, x, y, width, height, "imagej tiff");
	}

	/**
	 * Same as
	 * {@link #getImageBytes(ImageServer, double, int, int, int, int, int, int)},
	 * but encoded with the {@link Base64} scheme.
	 */
	public static String getImageBase64(
			ImageServer<BufferedImage> server,
			double downsample,
			int x, int y, int width, int height,
			int z, int t) throws IOException {
		return getImageBase64(server, downsample, x, y, width, height, z, t, "imagej tiff");
	}

	/**
	 * Same as {@link #getImageBytes(ImageServer, RegionRequest)}, but encoded with
	 * the {@link Base64} scheme.
	 */
	public static String getImageBase64(
			ImageServer<BufferedImage> server,
			RegionRequest request) throws IOException {
		return getImageBase64(server, request, "imagej tiff");
	}

	/**
	 * Set the image type of the image data.
	 *
	 * <p>
	 * If the image type is not set, it will be estimated.
	 * If the image type setting is set to {@link PathPrefs.ImageTypeSetting#PROMPT},
	 * a prompt will be displayed to set the image type.
	 * If the image type setting is set to {@link PathPrefs.ImageTypeSetting#AUTO_ESTIMATE},
	 * the image type will be automatically set.
	 * </p>
	 * @param imageData the image data to set the image type
	 */
	private static void setImageType(ImageData<BufferedImage> imageData) throws IOException {
		if ((imageData != null) && (imageData.getImageType() == null || imageData.getImageType() == ImageData.ImageType.UNSET)) {
			var setType = PathPrefs.imageTypeSettingProperty().get();
			if (setType == PathPrefs.ImageTypeSetting.AUTO_ESTIMATE || setType == PathPrefs.ImageTypeSetting.PROMPT) {
				ImageData.ImageType type = estimatedImageType(imageData.getServer());
				if (setType == PathPrefs.ImageTypeSetting.PROMPT) {
					ImageDetailsPane.promptToSetImageType(imageData, type);
				} else {
					imageData.setImageType(type);
					imageData.setChanged(false); // Don't want to retain this as a change resulting in a prompt to save the data
				}
			}
		}
	}

	/**
	 * Estimate the image type of the image server.
	 *
	 * @param server the image server to estimate the image type
	 * @return the estimated image type
	 * @throws IOException if an error occurs while estimating the image type
	 * @see GuiTools#estimateImageType(ImageServer, BufferedImage)
	 */
	private static ImageData.ImageType estimatedImageType(ImageServer<BufferedImage> server) throws IOException {
		return GuiTools.estimateImageType(server, server.getDefaultThumbnail(0,0));
	}

	/**
	 * Refresh the thumbnail of the image entry.
	 *
	 * @param entry  the image entry to refresh the thumbnail
	 * @param server the image server to get the thumbnail from
	 * @throws IOException if an error occurs while getting the thumbnail
	 * @see ProjectCommands#getThumbnailRGB(ImageServer)
	 * @see ProjectImageEntry#setThumbnail(BufferedImage)
	 */
	private static void refreshThumbnail(
			ProjectImageEntry<BufferedImage> entry,
			ImageServer<BufferedImage> server) throws IOException {
		entry.setThumbnail(ProjectCommands.getThumbnailRGB(server));
	}

}
