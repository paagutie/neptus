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
 * Author: zp
 * Jul 17, 2015
 */
package pt.lsts.wgcontrol;

import java.util.LinkedHashMap;

import pt.lsts.neptus.types.vehicle.VehicleType;
import pt.lsts.neptus.types.vehicle.VehiclesHolder;

/**
 * @author zp
 *
 */
public class WgNetworking {

    static LinkedHashMap<String, VehicleType> wgliders = new LinkedHashMap<String, VehicleType>();
    static {
        String[] ids = VehiclesHolder.getVehiclesArray();
        for (String id : ids) {
            if ("LRI Wave Glider".equals(VehiclesHolder.getVehicleById(id).getModel()))
                wgliders.put(VehiclesHolder.getVehicleById(id).getId(), VehiclesHolder.getVehicleById(id));            
        }
    }
    public static void sendCommand(String wgName, String command) throws Exception {
        if (!wgliders.containsKey(wgName))
            throw new Exception("Vehicle not valid, one of "+wgliders.keySet()+" expected.");
        
        System.out.println(wgliders.get(wgName).getProtocols());
    }
    
    public static void parseReply(String source, String reply) {
        
    }
    
    public static void parseTelemetry(String source, String telemetry) {
        //publish EstimatedState
        //publish FuelLevel
        //publish DesiredPath
    }
    
    public static void main(String[] args) throws Exception {
        
        WgNetworking.sendCommand("waveglider-lisa", "ss");
    }
}
