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
 * Author: hfq
 * Apr 9, 2013
 */
package pt.lsts.neptus.vtk.visualization;

import vtk.vtkActor;
import vtk.vtkCubeAxesActor;
import vtk.vtkPolyData;
import vtk.vtkRenderer;

/**
 * @author hfq FIXME - Grid is enabled by default, check better solution
 */
public class CubeAxes {

    private vtkCubeAxesActor cubeAxesActor;

    public CubeAxes() {

    }

    public vtkActor AddCubeAxesToVisualizer(vtkRenderer renderer, vtkPolyData polyData) {

        cubeAxesActor = new vtkCubeAxesActor();

        cubeAxesActor.SetCamera(renderer.GetActiveCamera());
        cubeAxesActor.SetBounds(polyData.GetBounds());
        cubeAxesActor.SetXTitle("X axis");
        // cubeAxesActor.SetXAxisMinorTickVisibility(0);
        // cubeAxesActor.SetDrawXGridlines(0);
        cubeAxesActor.DrawXGridlinesOn();
        cubeAxesActor.SetXUnits("m");

        // cubeAxesActor.SetAxisLabes(0, getXLabels()); nao tem o método, falta impletar o getXLabels
        cubeAxesActor.SetYTitle("Y axis");
        // cubeAxesActor.SetYAxisLabelVisibility(0);
        // cubeAxesActor.SetYAxisMinorTickVisibility(0);
        cubeAxesActor.DrawYGridlinesOn();
        cubeAxesActor.SetYUnits("m");

        // cubeAxesActor.SetAxisLabes(0, getYLabels()); nao tem o método, falta impletar o getYLabels
        cubeAxesActor.SetZTitle("Z axis");
        // cubeAxesActor.SetZAxisMinorTickVisibility(0);
        cubeAxesActor.DrawZGridlinesOn();
        cubeAxesActor.SetZUnits("m");

        // propriedades do actor
        // cubeAxesActor.GetProperty().SetDiffuseColor(0.0, 1.0, 0.0);
        cubeAxesActor.GetProperty().SetColor(0.0, 1.0, 0.0);
        // cubeAxesActor.GetProperty().SetInterpolationToPhong();

        return cubeAxesActor;
    }

}
