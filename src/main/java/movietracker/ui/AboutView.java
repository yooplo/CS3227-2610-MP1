package movietracker.ui;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.SVGPath;
import javafx.scene.transform.Scale;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/** Static application and attribution information. */
final class AboutView extends VBox {
    private static final double LOGO_SOURCE_WIDTH = 489.04;
    private static final double LOGO_SOURCE_HEIGHT = 35.4;
    private static final double LOGO_DISPLAY_WIDTH = 260.0;

    AboutView() {
        getStyleClass().addAll("section-view", "about-view");
        setPadding(new Insets(28));
        setSpacing(22);

        Label heading = new Label("About");
        heading.getStyleClass().add("section-heading");

        VBox applicationCard = new VBox(7);
        applicationCard.getStyleClass().add("about-card");
        Label appName = new Label("Movie Tracker");
        appName.getStyleClass().add("about-app-name");
        Label purpose = wrappingLabel(
                "A desktop application for discovering movies and managing a local "
                        + "Watchlist and Watched collection.",
                "about-copy");
        applicationCard.getChildren().addAll(appName, purpose);

        VBox creditsCard = new VBox(12);
        creditsCard.getStyleClass().add("about-card");
        Label creditsHeading = new Label("Movie data and images");
        creditsHeading.getStyleClass().add("credits-heading");
        Label suppliedBy = wrappingLabel(
                "Movie metadata and images displayed by Movie Tracker are supplied by TMDB.",
                "about-copy");
        Label notice = wrappingLabel(TmdbAttribution.NOTICE, "attribution-notice");
        creditsCard.getChildren().addAll(creditsHeading, suppliedBy, createOfficialTmdbLogo(), notice);

        getChildren().addAll(heading, applicationCard, creditsCard);
    }

    private static Label wrappingLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setWrapText(true);
        label.setMaxWidth(680);
        return label;
    }

    private static Pane createOfficialTmdbLogo() {
        try (InputStream stream = AboutView.class.getResourceAsStream(TmdbAttribution.LOGO_RESOURCE)) {
            if (stream == null) {
                throw new IllegalStateException("The bundled TMDB attribution logo is missing");
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(stream);

            SVGPath path = new SVGPath();
            path.setContent(document.getElementsByTagName("path").item(0).getAttributes()
                    .getNamedItem("d").getNodeValue());
            NodeList stops = document.getElementsByTagName("stop");
            path.setFill(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, javafx.scene.paint.Color.web(stopColor(stops, 0))),
                    new Stop(0.56, javafx.scene.paint.Color.web(stopColor(stops, 1))),
                    new Stop(1, javafx.scene.paint.Color.web(stopColor(stops, 2)))));

            double scale = LOGO_DISPLAY_WIDTH / LOGO_SOURCE_WIDTH;
            path.getTransforms().add(new Scale(scale, scale));
            Group group = new Group(path);
            Pane container = new Pane(group);
            container.getStyleClass().add("tmdb-logo");
            container.setMinSize(LOGO_DISPLAY_WIDTH, LOGO_SOURCE_HEIGHT * scale);
            container.setPrefSize(LOGO_DISPLAY_WIDTH, LOGO_SOURCE_HEIGHT * scale);
            container.setMaxSize(LOGO_DISPLAY_WIDTH, LOGO_SOURCE_HEIGHT * scale);
            return container;
        } catch (IOException | ParserConfigurationException | SAXException exception) {
            throw new IllegalStateException("The bundled TMDB attribution logo is invalid", exception);
        }
    }

    private static String stopColor(NodeList stops, int index) {
        return stops.item(index).getAttributes().getNamedItem("stop-color").getNodeValue();
    }
}
