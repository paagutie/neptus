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
 * Author: jqcorreia
 * Nov 20, 2013
 */
package pt.lsts.neptus.mra.api;


/**
 * @author jqcorreia
 *
 */
public class SidescanParameters {
    private double normalization = 0.2;
    private double tvgGain = 75;
    private double minValue = 0.0;
    private double windowValue = 1.0;
    
    /**
     * @param normalization
     * @param tvgGain
     */
    public SidescanParameters(double normalization, double tvgGain) {
        this.normalization = normalization;
        this.tvgGain = tvgGain;
    }

    public SidescanParameters(double normalization, double tvgGain, double minValue, double windowValue) {
        this.normalization = normalization;
        this.tvgGain = tvgGain;
        this.minValue = minValue;
        this.windowValue = windowValue;
    }

    /**
     * @return the normalization
     */
    public double getNormalization() {
        return normalization;
    }

    /**
     * @param normalization the normalization to set
     */
    public void setNormalization(double normalization) {
        this.normalization = normalization;
    }

    /**
     * @return the tvgGainpublic
     */
    public double getTvgGain() {
        return tvgGain;
    }

    /**
     * @param tvgGain the tvgGain to set
     */
    public void setTvgGain(double tvgGain) {
        this.tvgGain = tvgGain;
    }

    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    public double getWindowValue() {
        return windowValue;
    }

    public void setWindowValue(double windowValue) {
        this.windowValue = windowValue;
    }
}
