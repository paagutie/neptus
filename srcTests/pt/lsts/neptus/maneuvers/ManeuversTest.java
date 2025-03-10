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
 * Author: Hugo Dias
 * Oct 20, 2011
 */
package pt.lsts.neptus.maneuvers;

import java.util.LinkedHashMap;

import org.junit.Assert;
import org.junit.Test;

import pt.lsts.imc.IMCMessage;
import pt.lsts.imc.IMCUtil;
import pt.lsts.neptus.NeptusLog;
import pt.lsts.neptus.mp.Maneuver;
import pt.lsts.neptus.mp.ManeuverFactory;
import pt.lsts.neptus.mp.maneuvers.IMCSerialization;
import pt.lsts.neptus.types.vehicle.VehiclesHolder;
import pt.lsts.neptus.util.conf.ConfigFetch;

/**
 * @author ZP
 *
 */
public class ManeuversTest {

    @Test
    public void testImcSerialization() {
        ConfigFetch.initialize();
        ManeuverFactory fac = VehiclesHolder.getVehicleById("lauv-xtreme-2").getManeuverFactory();

        for (String man : fac.getAvailableManeuversIDs()) {
            Maneuver maneuver = fac.getManeuver(man);
            if (maneuver instanceof IMCSerialization) {
                NeptusLog.pub().info("<###>now testing "+maneuver.getType());
                IMCSerialization ser = ((IMCSerialization) maneuver);
                IMCMessage random = ser.serializeToIMC();
                if (random.getTypeOf("custom") != null) {
                    LinkedHashMap<String, String> custom = random.getTupleList("custom");
                    IMCUtil.fillWithRandomData(random);
                    if (!custom.isEmpty())
                        random.setValue("custom", custom);
                }
                else {
                    IMCUtil.fillWithRandomData(random);
                }
                String before = random.asXml(true);
                NeptusLog.pub().info("<###> "+before);
                ((IMCSerialization) maneuver).parseIMCMessage(random);
                NeptusLog.pub().info("<###> "+maneuver.asXML());
                String after = ser.serializeToIMC().asXml(true);                
                Assert.assertEquals(before, after);                
            }
        }
    }

}
