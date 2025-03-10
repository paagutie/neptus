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
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
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
 * Author: meg
 * May 19, 2014
 */
package pt.lsts.neptus.plugins.wg;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Date;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;

import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.colormap.ColorBar;
import pt.lsts.neptus.colormap.ColorMap;
import pt.lsts.neptus.colormap.ColorMapFactory;
import pt.lsts.neptus.console.ConsoleLayer;
import pt.lsts.neptus.gui.PropertiesProvider;
import pt.lsts.neptus.plugins.NeptusProperty;
import pt.lsts.neptus.plugins.NeptusProperty.LEVEL;
import pt.lsts.neptus.plugins.PluginDescription;
import pt.lsts.neptus.renderer2d.LayerPriority;
import pt.lsts.neptus.renderer2d.StateRenderer2D;
import pt.lsts.neptus.types.coord.LocationType;

/**
 * @author Margarida
 * 
 */
@PluginDescription(name = "Wave Glider CTD", icon = "pt/lsts/neptus/plugins/wg/icons/ctd.png")
@LayerPriority(priority = 200)
public class WgCtdLayer extends ConsoleLayer implements PropertiesProvider {

    @NeptusProperty(name = "Start date", description = "Starting date ctd data was collected", userLevel = LEVEL.REGULAR)
    private Date dataDateStart = new Date(System.currentTimeMillis() - 86400000);

    @NeptusProperty(name = "End date", description = "Ending date ctd data was collected", userLevel = LEVEL.REGULAR)
    private Date dataDateEnd = new Date();

    @NeptusProperty(name = "url", description = "Url generated by Liquid Robotics to access cdt data", userLevel = LEVEL.REGULAR)
    private String url = "http://slab.liquidr.com/fetch?t=liTH3Yc8yxtG2y9";
    
    @NeptusProperty(name = "Color map", description = "Colormap to be used while plotting CTD", userLevel = LEVEL.REGULAR, editorClass=pt.lsts.neptus.gui.editor.ColorMapPropertyEditor.class)
    private ColorMap cmap = ColorMapFactory.createJetColorMap();
    
    private ColorBar cbar = new ColorBar(ColorBar.VERTICAL_ORIENTATION);
    
    private final TreeMap<Long, CtdData> ctdHistory = new TreeMap<Long, CtdData>();

    // GUI variables
    private double maxTemp = 0;
    private double minTemp = 0;
    private boolean doneLoading;

    @Override
    public boolean userControlsOpacity() {
        return true;
    }

    @Override
    public void initLayer() {
        doneLoading = false;
        Runnable r = new Runnable() {
            @Override
            public void run() {
                if (!fetchHistory(url)) {
                    NeptusLog.pub().error("Problem loading  history from Liquid Robotics");
                }
                else {
                    doneLoading = true;
                }
            }
        };

        new Thread(r).start();
    }

    @Override
    public void cleanLayer() {
    }

    @Override
    public void paint(Graphics2D g, StateRenderer2D renderer) {
        super.paint(g, renderer);
        if (doneLoading) {
            if (paintHistory(g, renderer)) {
                paintCaption(g);
            }
        }
        else {
            System.out.println("Waiting");
        }
    }

    private boolean paintHistory(Graphics2D g, StateRenderer2D renderer) {
        boolean paintedToRender = false;
        Long endMark = dataDateEnd.getTime();
        Long timeMark = dataDateStart.getTime();
        CtdData dataPoint;
        // double tempMaxTemp, tempMinTemp;
        // timeMark = ctdHistory.higherKey(timeMark);
        // if (timeMark != null) {
        // dataPoint = ctdHistory.get(timeMark);
        // tempMaxTemp = dataPoint.temperature;
        // tempMinTemp = dataPoint.temperature;
        // }
        // else {
        // tempMaxTemp = maxTemp;
        // tempMinTemp = minTemp;
        // }
        Color color;
        Graphics2D gt;
        Point2D pt;
        int width = (renderer.getZoom() > 1) ? (int) (5 * renderer.getZoom()) : 5;
        if (renderer.getZoom() < 0.002) {
            timeMark = ctdHistory.higherKey(timeMark);
            if (timeMark != null && timeMark <= endMark) {
                dataPoint = ctdHistory.get(timeMark);
                pt = renderer.getScreenPosition(dataPoint.location);
                if (isVisibleInRender(pt, renderer)) {
                    gt = (Graphics2D) g.create();
                    color = genColor(dataPoint.temperature);
                    gt.setColor(color);
                    gt.fillOval((int) pt.getX(), (int) pt.getY(), 10, 10);
                    gt.dispose();
                    paintedToRender = true;
                }
            }
        }
        else {
            try {
                timeMark = ctdHistory.higherKey(timeMark);
                // TODO avoid for in case of being too far from location shown on map
                for (timeMark = ctdHistory.higherKey(timeMark); 
                        timeMark != null && timeMark <= endMark; 
                        timeMark = ctdHistory.higherKey(timeMark+1)){
                    dataPoint = ctdHistory.get(timeMark);
                    pt = renderer.getScreenPosition(dataPoint.location);
                    if (!isVisibleInRender(pt, renderer)) {
                        continue;
                    }
                    // check if it is visible
                    gt = (Graphics2D) g.create();
                    color = genColor(dataPoint.temperature);
                    gt.setColor(color);
                    gt.fillOval((int) pt.getX(), (int) pt.getY(), width, width);
                    gt.dispose();
                    paintedToRender = true;
                }
            }
            catch (NullPointerException e) {
                System.out.println("timeMark");
            }
        }
        return paintedToRender;
    }

    private boolean fetchHistory(String url) {
        try {
            URL urlAssets = new URL(url);
            // File tmp = File.createTempFile("neptus", "assets");
            File tmp = new File("ctdLR.txt");
            FileUtils.copyURLToFile(urlAssets, tmp);

            BufferedReader reader = new BufferedReader(new FileReader(tmp));

            String line = reader.readLine(); // header line

            boolean init = true;

            while ((line = reader.readLine()) != null) {
                String parts[] = line.split(",");
                //System.out.println(line);
                LocationType loc = new LocationType(Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1]
                        .trim()));
                long timestamp = Long.parseLong(parts[2].trim());
                CtdData dataPoint = new CtdData(loc, timestamp, Double.parseDouble(parts[3].trim()),
                        Double.parseDouble(parts[6].trim()), Double.parseDouble(parts[7].trim()),
                        Double.parseDouble(parts[8].trim()), Double.parseDouble(parts[9].trim()));
                ctdHistory.put(timestamp, dataPoint);

                if (init) {
                    minTemp = maxTemp = dataPoint.temperature;
                    init = false;
                }
                else {
                    if (dataPoint.temperature < minTemp)
                        minTemp = dataPoint.temperature;
                    if (dataPoint.temperature > maxTemp)
                        maxTemp = dataPoint.temperature;
                }
            }

            reader.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Color genColor(double temp) {
        double normalizedVal = (temp - minTemp) / (maxTemp - minTemp);
        return cmap.getColor(normalizedVal);
    }

    private void paintCaption(Graphics2D g) {
        cbar.setCmap(cmap);
        cbar.setSize(new Dimension(20, 120));
        Graphics copy = g.create();
        copy.translate(10, 10);
        cbar.paint(copy);
        copy.setColor(Color.white);
        copy.drawString(String.format("%3.2f ºC",  minTemp), 23, 120);
        copy.drawString(String.format("%3.2f ºC",  maxTemp), 23, 10);
    }

    private boolean isVisibleInRender(Point2D sPos, StateRenderer2D renderer) {
        Dimension rendDim = renderer.getSize();
        if (sPos.getX() < 0 && sPos.getY() < 0)
            return false;
        else if (sPos.getX() > rendDim.getWidth() && sPos.getY() > rendDim.getHeight())
            return false;

        return true;
    }
}
