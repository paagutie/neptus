/*
 * Copyright (c) 2004-2023 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * Modified European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the Modified EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENCE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://github.com/LSTS/neptus/blob/develop/LICENSE.md
 * and http://ec.europa.eu/idabc/eupl.html.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: José Pinto
 * Jul 12, 2012
 */
package pt.lsts.neptus.plugins.ais;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.google.gson.Gson;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.console.ConsoleLayout;
import pt.lsts.neptus.gui.PropertiesEditor;
import pt.lsts.neptus.i18n.I18n;
import pt.lsts.neptus.planeditor.IEditorMenuExtension;
import pt.lsts.neptus.planeditor.IMapPopup;
import pt.lsts.neptus.plugins.NeptusProperty;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.plugins.SimpleRendererInteraction;
import pt.lsts.neptus.plugins.update.IPeriodicUpdates;
import pt.lsts.neptus.plugins.update.PeriodicUpdatesService;
import pt.lsts.neptus.renderer2d.LayerPriority;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.types.coord.LocationType;
import pt.lsts.neptus.util.GuiUtils;

/**
 * @author zp
 * 
 */
@PluginDescription(author = "ZP", name = "AIS Overlay", icon = "pt/lsts/neptus/plugins/ais/mt.png")
@LayerPriority(priority=-50)
public class AisOverlay extends SimpleRendererInteraction implements IPeriodicUpdates, IEditorMenuExtension {
    private static final long serialVersionUID = 1L;

    @NeptusProperty(name = "Show vessel names")
    public boolean showNames = true;

    @NeptusProperty(name = "Show vessel speeds")
    public boolean showSpeeds = true;

    @NeptusProperty(name = "Milliseconds between vessel updates")
    public long updateMillis = 60000;

    @NeptusProperty(name = "Show only when selected")
    public boolean showOnlyWhenInteractionIsActive = true;

    @NeptusProperty(name = "Show stationary vessels")
    public boolean showStoppedShips = false;

    @NeptusProperty(name = "Interpolate and predict positions")
    public boolean interpolate = false;
    
    @NeptusProperty(name = "Moving vessel color")
    public Color movingColor = Color.blue.darker();
    
    @NeptusProperty(name = "Stationary vessel color")
    public Color stationaryColor = Color.gray.darker();
    
    @NeptusProperty(name = "Vessel label color")
    public Color labelColor = Color.black;
    
    @NeptusProperty(name = "Show speed in knots")
    public boolean useKnots = false;

    protected boolean active = false;
    protected Vector<AisShip> shipsOnMap = new Vector<>();
    protected StateRenderer2D renderer = null;

    protected boolean updating = false;

    protected Thread lastThread = null;

    private final Gson gson = new Gson();

    protected GeneralPath path = new GeneralPath();
    {
        path.moveTo(0, 5);
        path.lineTo(-5, 3.5);
        path.lineTo(-5, -5);
        path.lineTo(5, -5);
        path.lineTo(5, 3.5);
        path.lineTo(0, 5);
        path.closePath();
    }

    static final SSLConnectionSocketFactory sslsf;
    static final Registry<ConnectionSocketFactory> registry;
    static final PoolingHttpClientConnectionManager cm;
    static {
        try {
            sslsf = new SSLConnectionSocketFactory(SSLContext.getDefault(),
                    NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new PlainConnectionSocketFactory())
                .register("https", sslsf)
                .build();
        cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(100);
    }

    /**
     * @param console The {@link ConsoleLayout} where this layer will reside.
     */
    public AisOverlay(ConsoleLayout console) {
        super(console);
    }

    @Override
    public boolean isExclusive() {
        return true;
    }

    @Override
    public void setActive(boolean mode, StateRenderer2D source) {
        super.setActive(mode, source);
        active = mode;
        if (active)
            update();
    }

    @Override
    public long millisBetweenUpdates() {
        return updateMillis;
    }

    @Override
    public boolean update() {

        if (showOnlyWhenInteractionIsActive && !active)
            return true;
        // don't let more than one thread be running at a time
        if (lastThread != null)
            return true;

        lastThread = new Thread(() -> {
            updating = true;
            try {

                if (renderer == null) {
                    lastThread = null;
                    return;
                }

                LocationType topLeft = renderer.getTopLeftLocationType();
                LocationType bottomRight = renderer.getBottomRightLocationType();

                shipsOnMap = getShips(bottomRight.getLatitudeDegs(), topLeft.getLongitudeDegs(),
                        topLeft.getLatitudeDegs(), bottomRight.getLongitudeDegs(), showStoppedShips);

                lastThread = null;


                renderer.repaint();
            }
            finally {
                updating = false;
            }
        });
        lastThread.setName("AIS Fetcher thread");
        lastThread.setDaemon(true);
        lastThread.start();

        return true;
    }

    protected Vector<AisShip> getShips(double minLat, double minLon, double maxLat, double maxLon,
            boolean includeStationary) {
        Vector<AisShip> ships = new Vector<>();

        // area is too large
        if (maxLat - minLat > 2)
            return ships;

        try (CloseableHttpClient client = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionManager(cm)
                .build()) {
            URL url = new URL("http://www.marinetraffic.com/ais/getjson.aspx?sw_x=" + minLon + "&sw_y=" + minLat
                    + "&ne_x=" + maxLon + "&ne_y=" + maxLat + "&zoom=12" + "&fleet=&station=0&id=null");

            HttpGet get = new HttpGet(url.toURI());
            get.setHeader("Referer", "http://www.marinetraffic.com/ais/");

            try (CloseableHttpResponse response = client.execute(get);
                 BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                String json = rd.lines().collect(Collectors.joining());
                String[][] res = gson.fromJson(json, String[][].class);

                for (String[] re : res) {
                    double knots = Double.parseDouble(re[5]) / 10;
                    if (!includeStationary && knots <= 0.2)
                        continue;

                    AisShip ship = new AisShip();
                    ship.setLatitude(Double.parseDouble(re[0]));
                    ship.setLongitude(Double.parseDouble(re[1]));
                    ship.setName(re[2]);
                    ship.setMMSI(Integer.parseInt(re[7]));
                    ship.setSpeed(knots);
                    if (re[4] != null)
                        ship.setCourse(Double.parseDouble(re[4]));
                    if (re[6] != null)
                        ship.setCountry(re[6]);
                    if (re[8] != null)
                        ship.setLength(Double.parseDouble(re[8]));
                    ships.add(ship);
                }
            }
            catch (Exception e) {
                NeptusLog.pub().warn(e);
            }
        }
        catch (Exception e) {
            NeptusLog.pub().warn(e);
        }
        return ships;
    }

    @Override
    public void cleanSubPanel() {
        PeriodicUpdatesService.unregister(this);
        shipsOnMap.clear();
    }

    @Override
    public void mouseClicked(MouseEvent event, StateRenderer2D source) {
        super.mouseClicked(event, source);

        JPopupMenu popup = new JPopupMenu();
        popup.add(I18n.text("AIS settings")).addActionListener(e -> PropertiesEditor.editProperties(AisOverlay.this, getConsole(), true));

        popup.add(I18n.text("Update ships")).addActionListener(e -> update());

        popup.add(getShipInfoMenu());

        popup.show(source, event.getX(), event.getY());
    }

    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        super.paint(g, renderer);
        this.renderer = renderer;

        if (showOnlyWhenInteractionIsActive && !active)
            return;

        if (lastThread != null) {
            //String strUpdateAIS = I18n.text("Updating AIS layer...");
            g.drawString(I18n.text("Updating AIS layer..."), 10, 15);
        }
        else {
            //String strVisShips = I18n.text(" visible ships");
            g.drawString(I18n.textf("%numberOfShips visible ships", shipsOnMap.size()), 10, 15);
        }

        for (AisShip ship : shipsOnMap) {
            Graphics2D clone = (Graphics2D) g.create();
            LocationType shipLoc = ship.getLocation();
            if (interpolate) {
                double dT = (System.currentTimeMillis() - ship.lastUpdate) / 1000.0;
                double tx = Math.cos(ship.getHeadingRads()) * dT * ship.getSpeedMps();
                double ty = Math.sin(ship.getHeadingRads()) * dT * ship.getSpeedMps();
                shipLoc.translatePosition(tx, ty, 0);
            }
            Point2D pt = renderer.getScreenPosition(shipLoc);
            clone.translate(pt.getX(), pt.getY());
            Graphics2D clone2 = (Graphics2D) clone.create();

            Color c = movingColor;
            if (ship.getSpeedKnots() <= 0.2)
                c = stationaryColor;

            double scaleX = (renderer.getZoom() / 10) * ship.getLength() / 9;
            double scaleY = (renderer.getZoom() / 10) * ship.getLength();

            clone.rotate(Math.PI + ship.getHeadingRads() - renderer.getRotation());
            clone.setColor(c.brighter());//new Color(c.getRed(), c.getGreen(), c.getBlue(), 128));
            clone.setStroke(new BasicStroke(1.0f,
                    BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER,
                    10.0f, new float[]{3f, 10f}, 0.0f));
            clone.draw(new Line2D.Double(0, ship.getLength() / 1.99 * renderer.getZoom(), 0, ship.getSpeedMps() * 60
                    * renderer.getZoom()));
            clone.scale(scaleX, scaleY);
            clone.setColor(c);
            clone.fill(path);
            clone.dispose();

            //clone2.rotate(-Math.PI/2 + ship.getHeadingRads());
            clone2.setFont(new Font("Helvetica", Font.PLAIN, 8));
            clone2.setColor(labelColor);
            clone2.drawLine(-3, 0, 3, 0);
            clone2.drawLine(0, -3, 0, 3);
            if (showNames) {
                clone2.drawString(ship.getName(), 5, 5);
            }

            if (showSpeeds && ship.getSpeedKnots() > 0.2) {
                if (useKnots)
                    clone2.drawString(GuiUtils.getNeptusDecimalFormat(1).format(ship.getSpeedKnots()) + " kn", 5, 15);
                else
                    clone2.drawString(GuiUtils.getNeptusDecimalFormat(1).format(ship.getSpeedMps()) + " m/s", 5, 15);
            }
            clone2.dispose();
        }
    }
    
    protected JMenu getShipInfoMenu() {
        JMenu menu = new JMenu(I18n.text("Ship Info"));
        Vector<AisShip> ships = new Vector<>(shipsOnMap);
        Collections.sort(ships);

        if (ships.size() > 0 && Desktop.isDesktopSupported()) {
            for (final AisShip s : ships) {
                menu.add(s.getName()).addActionListener(e -> {
                    Desktop desktop = Desktop.getDesktop();
                    try {
                        URI uri = new URI(s.getShipInfoURL());
                        desktop.browse(uri);
                    }
                    catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        }
        else {
            menu.setEnabled(false);
        }
        
        return menu;
    }
        
    @Override
    public Collection<JMenuItem> getApplicableItems(LocationType loc, IMapPopup source) {
        Vector<JMenuItem> items = new Vector<>();
        
        if (!shipsOnMap.isEmpty())
            items.add(getShipInfoMenu());
        
        return items;
    }

    @Override
    public void initSubPanel() {
        Vector<IMapPopup> r = getConsole().getSubPanelsOfInterface(IMapPopup.class);
        for (IMapPopup str2d : r) {
            str2d.addMenuExtension(this);
        }
    }

}
