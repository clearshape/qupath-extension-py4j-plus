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
import java.util.Base64;


/**
 * Add more useful methods on top of {@link QuPathEntryPoint}.
 */
public class QuPathEntryPoint2 extends QuPathEntryPoint {

	/**
	 * Refresh the current project {@link QPEx#getProject() getProject()} in QuPath.
	 *
	 * @see QuPathGUI#refreshProject()
	 */
	public static void refreshProjectInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			getQuPath().refreshProject();
			return null;
		});
	}

	/**
	 * Repaint the entire image in the current Viewer {@link QPEx#getCurrentViewer() getCurrentViewer()}.
	 *
	 * @see QuPathViewer#repaintEntireImage()
	 */
	public static void repaintEntireImageInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			getCurrentViewer().repaintEntireImage();
			return null;
		});
	}

	/**
	 * Open a project in QuPath.
	 *
	 * @param project the project to open
	 *
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
	 *
	 * @see Commands#closeProject(QuPathGUI)
	 */
	public static void closeProjectInQuPath() {
		FXUtils.callOnApplicationThread(() -> {
			Commands.closeProject(getQuPath());
			return null;
		});
	}

	/**
	 * Open image <code>imageData</code> in QuPath.
	 * The current project will be closed
	 * if it does not contain <code>imageData</code>
	 *
	 * @param imageData the image to open
	 *
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
	 * Close the current image {@link QPEx#getCurrentImageData() getCurrentImageData()} in QuPath.
	 *
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

	/**
	 * Create a new project at path <code>projectPath</code>.
	 * The existing contents of <code>projectPath</code> will be erased.
	 *
	 * @param projectPath the path to the project
	 * @return the created project
	 */
	public static Project<BufferedImage> createProject(String projectPath) {
		mkdirs(projectPath);
		return Projects.createProject(new File(projectPath), BufferedImage.class);
	}

	/**
	 * Save project <code>project</code> to its associated path
	 * <code>project</code>.{@link Project#getPath() getPath()}.
	 *
	 * @param project the project to save
	 * @throws IOException if an error occurs while saving the project
	 *
	 * @see Project#syncChanges()
	 * @see QuPathEntryPoint2#createProject(String)
	 */
	public static void saveProject(Project<BufferedImage> project) throws IOException {
		if (project != null) {
			project.syncChanges();
		}
	}

	/**
	 * Load a project from path <code>projectPath</code>.
	 * The project is defined by JSON file <code>projectPath/project.qpproj</code>.
	 *
	 * @param projectPath the path to the project
	 * @return the loaded project
	 * @throws IOException if an error occurs while loading the project
	 *
	 * @see ProjectIO#loadProject(File, Class)
	 */
	public static Project<BufferedImage> loadProject(String projectPath) throws IOException {
		File file = new File(projectPath);
		if (file.isDirectory()) {
			file = new File(file, ProjectIO.DEFAULT_PROJECT_NAME + ProjectIO.getProjectExtension(true));
		}
		return ProjectIO.loadProject(file, BufferedImage.class);
	}

	/**
	 * Add an image server <code>server</code> to project <code>project</code>.
	 * An image entry is returned and its thumbnail is refreshed.
	 *
	 * @param project the project to add the image entry to
	 * @param server  the image server used to create the image entry
	 * @return the added image entry
	 * @throws IOException if an error occurs while adding the image entry
	 *
	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
	 *      ImageData.ImageType)
	 * @see QuPathEntryPoint2#refreshThumbnail(ProjectImageEntry, ImageServer)
	 * @see QuPathEntryPoint2#estimatedImageType(ImageServer)
	 */
	public static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			ImageServer<BufferedImage> server) throws IOException {
		return addImageEntry(project, server, estimatedImageType(server));
	}

	/**
	 * Add an image file <code>imagePath</code> to project <code>project</code>.
	 * An image entry is returned and its thumbnail is refreshed.
	 *
	 * @param project the project to add the image entry to
	 * @param imagePath  the image file used to create the image entry
	 * @return the added image entry
	 * @throws IOException if an error occurs while adding the image entry
	 *
	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
	 *      ImageData.ImageType)
	 * @see ImageServers#buildServer(String, String...)
	 * @see QuPathEntryPoint2#refreshThumbnail(ProjectImageEntry, ImageServer)
	 * @see QuPathEntryPoint2#estimatedImageType(ImageServer)
	 */
	public static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			String imagePath) throws IOException {
		return addImageEntry(project, ImageServers.buildServer(imagePath));
	}

	/**
	 * Remove image entry <code>entry</code> from project <code>project</code>.
	 * All associated data will be removed.
	 *
	 * @param project the project to remove the image entry from
	 * @param entry   the image entry to remove
	 *
	 * @see Project#removeImage(ProjectImageEntry, boolean)
	 */
	public static void removeImageEntry(
			Project<BufferedImage> project,
			ProjectImageEntry<BufferedImage> entry) {
		project.removeImage(entry, true);
	}

	/**
	 * Create a new image data using image server <code>server</code>.
	 *
	 * @param server the image server to create the image data from
	 * @return the created image data
	 *
	 * @see QuPathEntryPoint2#estimatedImageType(ImageServer)
	 */
	public static ImageData<BufferedImage> createImageData(ImageServer<BufferedImage> server) throws IOException {
		return createImageData(server, estimatedImageType(server));
	}

	/**
	 * Create a new image data using image file <code>imagePath</code>.
	 *
	 * @param imagePath the image file to create the image data from
	 * @return the created image data
	 *
	 * @see QuPathEntryPoint2#estimatedImageType(ImageServer)
	 * @see ImageServers#buildServer(String, String...)
	 */
	public static ImageData<BufferedImage> createImageData(String imagePath) throws IOException {
		return createImageData(ImageServers.buildServer(imagePath));
	}

	/**
	 * Save image data <code>imageData</code> if it has been changed
	 * and belongs to the current project {@link QPEx#getProject() getProject()}.
	 *
	 * @param imageData the image data to save
	 * @throws IOException if an error occurs while saving the image data
	 *
	 * @see ImageData#isChanged()
	 * @see Project#getEntry(ImageData)
	 * @see ProjectImageEntry#saveImageData(ImageData)
	 */
	public static void saveImageData(ImageData<BufferedImage> imageData) throws IOException {
		var project = getProject();
		if ((project != null) && (imageData != null) && (imageData.isChanged())) {
			var entry = project.getEntry(imageData);
			if (entry != null) {
				entry.saveImageData(imageData);
			}
		}
	}

	/**
	 * Save the current image data {@link QPEx#getCurrentImageData() getCurrentImageData()}
	 * if it has been changed and belongs to the current project {@link QPEx#getProject() getProject()}.
	 *
	 * @throws IOException if an error occurs while saving the image data
	 *
	 * @see QuPathEntryPoint2#saveImageData(ImageData)
	 */
	public static void saveCurrentImageData() throws IOException {
		saveImageData(getCurrentImageData());
	}

	/**
	 * Create a new ImageJ image server for image file <code>imagePath</code>.
	 *
	 * @param imagePath the path to the image
	 * @return the created image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 *
	 * @see ImageJServerBuilder#buildServer(URI, String...)
	 * @see ImageJServer#dumpMetadata()
	 */
	public static ImageServer<BufferedImage> createImageJImageServer(String imagePath) throws URISyntaxException {
		return new ImageJServerBuilder().buildServer(new URI(imagePath));
	}

	/**
	 * Create a new BioFormats image server for image file <code>imagePath</code>.
	 *
	 * @param imagePath the path to the image
	 * @return the created image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 *
	 * @see BioFormatsServerBuilder#buildServer(URI, String...)
	 * @see BioFormatsImageServer#dumpMetadata()
	 */
	public static ImageServer<BufferedImage> createBioFormatsImageServer(String imagePath) throws URISyntaxException {
		return new BioFormatsServerBuilder().buildServer(new URI(imagePath));
	}

	/**
	 * Create a new Openslide image server for image file <code>imagePath</code>.
	 *
	 * @param imagePath the path to the image
	 * @return the created image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 *
	 * @see OpenslideServerBuilder#buildServer(URI, String...)
	 * @see OpenslideImageServer#dumpMetadata()
	 */
	public static ImageServer<BufferedImage> createOpenslideImageServer(String imagePath) throws URISyntaxException {
		return new OpenslideServerBuilder().buildServer(new URI(imagePath));
	}

	/**
	 * Create a new image server for image file <code>imagePath</code>.
	 * QuPath will choose correct {@link ImageServerBuilder} to read <code>imagePath</code>.
	 *
	 * @param imagePath the path to the image
	 * @return the created image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 * @throws IOException if an error occurs while loading the image file
	 *
	 * @see ImageServers#buildServer(URI, String...)
	 */
	public static ImageServer<BufferedImage> createImageServer(String imagePath) throws URISyntaxException, IOException {
		return ImageServers.buildServer(new URI(imagePath));
	}

	/**
	 * Save image server <code>server</code> to JSON file <code>jsonServerPath</code>.
	 *
	 * @param server         the image server to save
	 * @param jsonServerPath the path to save the image server to
	 * @throws IOException if an error occurs while saving the image server
	 *
	 * @see FileWriter#write(String)
	 * @see GsonTools#getInstance()
	 * @see Gson#toJson(Object)
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
	 * Load an image server from a JSON file <code>jsonServerPath</code>.
	 *
	 * @param jsonServerPath the path to the JSON file
	 * @return the loaded image server
	 * @throws URISyntaxException if the image path is not a valid {@link URI}
	 * @throws IOException if an error occurs while loading the image file
	 *
	 * @see ImageServers#buildServer(URI, String...)
	 */
	public static ImageServer<BufferedImage> loadImageServer(String jsonServerPath) throws URISyntaxException, IOException {
		return ImageServers.buildServer(new URI(jsonServerPath));
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
	 * Set the image type of image data <code>imageData</code>.
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
	public static void setImageType(ImageData<BufferedImage> imageData) throws IOException {
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
	 * Estimate the image type of image server <code>server</code>
	 * using its default thumbnail.
	 *
	 * @param server the image server to estimate the image type
	 * @return the estimated image type
	 * @throws IOException if an error occurs while estimating the image type
	 *
	 * @see ImageServer#getDefaultThumbnail(int, int)
	 * @see GuiTools#estimateImageType(ImageServer, BufferedImage)
	 */
	public static ImageData.ImageType estimatedImageType(ImageServer<BufferedImage> server) throws IOException {
		return GuiTools.estimateImageType(server, server.getDefaultThumbnail(0,0));
	}

	/**
	 * Refresh the thumbnail of the image entry.
	 *
	 * @param entry  the image entry to refresh the thumbnail
	 * @param server the image server to get the thumbnail from
	 * @throws IOException if an error occurs while getting the thumbnail
	 *
	 * @see ProjectCommands#getThumbnailRGB(ImageServer)
	 * @see ProjectImageEntry#setThumbnail
	 */
	public static void refreshThumbnail(
			ProjectImageEntry<BufferedImage> entry,
			ImageServer<BufferedImage> server) throws IOException {
		entry.setThumbnail(ProjectCommands.getThumbnailRGB(server));
	}

	/**
	 * Add an image server <code>server</code> to project <code>project</code>
	 * with the specified image type <code>type</code>.
	 * An image entry is returned and its thumbnail is refreshed after adding it.
	 *
	 * @param project the project to add the image entry to
	 * @param server  the image server used to create the image entry
	 * @param type    {@link ImageData.ImageType} or null
	 * @return the added image entry
	 * @throws IOException if an error occurs while adding the image entry
	 *
	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
	 *      ImageData.ImageType)
	 */
	private static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			ImageServer<BufferedImage> server,
			ImageData.ImageType type) throws IOException {
		var entry = ProjectCommands.addSingleImageToProject(project, server, type);
		refreshThumbnail(entry, server);	// refresh its thumbnail
		return entry;
	}

	/**
	 * Add an image file <code>imagePath</code> to project <code>project</code>
	 * with the specified image type <code>type</code>.
	 * An image entry is returned and its thumbnail is refreshed after adding it.
	 *
	 * @param project   the project to add the image entry to
	 * @param imagePath the image file used to create the image entry
	 * @param type      {@link ImageData.ImageType} or null
	 * @return the added image entry
	 * @throws IOException if an error occurs while adding the image entry
	 *
	 * @see ProjectCommands#addSingleImageToProject(Project, ImageServer,
	 *      ImageData.ImageType)
	 */
	private static ProjectImageEntry<BufferedImage> addImageEntry(
			Project<BufferedImage> project,
			String imagePath,
			ImageData.ImageType type) throws IOException {
		return addImageEntry(project, ImageServers.buildServer(imagePath), type);
	}

	/**
	 * Create a new image data using image server <code>server</code>
	 * with the specified image type <code>type</code>.
	 *
	 * @param server the image server to create the image data from
	 * @param type the image type
	 * @return the created image data
	 */
	private static ImageData<BufferedImage> createImageData(
			ImageServer<BufferedImage> server,
			ImageData.ImageType type) throws IOException {
		return new ImageData<BufferedImage>(server, type);
	}

	/**
	 * Create a new image data using image file <code>imagePath</code>
	 * with the specified image type <code>type</code>.
	 *
	 * @param imagePath the image file to create the image data from
	 * @param type the image type
	 * @return the created image data
	 *
	 * @see ImageServers#buildServer(String, String...)
	 */
	private static ImageData<BufferedImage> createImageData(
			String imagePath,
			ImageData.ImageType type) throws IOException {
		return createImageData(ImageServers.buildServer(imagePath), type);
	}
}
