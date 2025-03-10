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
 * Author: Paulo Dias
 * 2011/11/19
 */
package pt.lsts.neptus.gui.system;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import org.jdesktop.swingx.JXPanel;

import pt.lsts.neptus.util.GuiUtils;

/**
 * @author pdias
 *
 */
@SuppressWarnings("serial")
public class LocationSymbol extends SymbolLabel {

	/* (non-Javadoc)
	 * @see pt.lsts.neptus.gui.system.SymbolLabel#initialize()
	 */
	@Override
	protected void initialize() {
		setSize(10, 10);
		setPreferredSize(new Dimension(10, 10));
		super.initialize();
	}
	
	/* (non-Javadoc)
	 * @see pt.lsts.neptus.gui.system.SymbolLabel#paint(java.awt.Graphics2D, org.jdesktop.swingx.JXPanel, int, int)
	 */
	@Override
	public void paint(Graphics2D g, JXPanel c, int width, int height) {
		Graphics2D g2 = (Graphics2D)g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.scale(width/10.0, height/10.0);
		
		RoundRectangle2D rect = new RoundRectangle2D.Double(0,0,10,10, 0,0);
		g2.setColor(new Color(0,0,0,0));
		g2.fill(rect);
		
		g2.setColor(getActiveColor());
		g2.setFont(new Font("Arial", Font.BOLD, 10));
		String tt = " Loc";
		Rectangle2D sB1 = g2.getFontMetrics().getStringBounds(tt, g2);
//			double scale;
		double sw0 = 10.0 / sB1.getWidth();
		double sh0 = 10.0 / sB1.getHeight();
//			scale = (sw0 < sh0)?sw0:sh0;
		g2.translate(4.3, 5);
		g2.scale(sw0, sh0);			
		g2.drawString(tt, (int) (-sB1.getWidth()/2.0), (int) (sB1.getHeight()/2.0));

		g2.scale(1/sw0, 1/sh0);         
        g2.translate(-3.6, -5);

        g2.setStroke(new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.translate(5, 2.5);
        g2.scale(0.7, 0.55);
        if (isActive()) {
            GeneralPath sp = new GeneralPath();
            sp.moveTo(1.45, 3);
            sp.lineTo(4, 3);
            sp.lineTo(0, -3.5);
            sp.lineTo(-4, 3);
            sp.lineTo(-1.45, 3);
            sp.closePath();
            g2.setColor(getActiveColor());
            g2.fill(sp);
            g2.draw(sp);
        }
        else {
            g2.drawLine(4, -3, -4, 3);
            g2.drawLine(4, 3, -4, -3);
        }
	}
	
	public static void main(String[] args) {
		LocationSymbol symb1 = new LocationSymbol();
		symb1.setActive(true);
		symb1.setSize(50, 50);
		JXPanel panel = new JXPanel();
		panel.setBackground(Color.BLACK);
		panel.setLayout(new BorderLayout());
		panel.add(symb1, BorderLayout.CENTER);
		GuiUtils.testFrame(panel,"",400,400);
		
//		try {Thread.sleep(5000);} catch (Exception e) {}
//		symb1.blink(true);
//		try {Thread.sleep(5000);} catch (Exception e) {}
//		symb1.blink(false);
//        try {Thread.sleep(5000);} catch (Exception e) {}
//        symb1.setActive(false);
//        try {Thread.sleep(5000);} catch (Exception e) {}
//        symb1.setActive(true);

	}
}
