/**
 *  Copyright (C) 2002-2018   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.client.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;

import net.sf.freecol.client.ClientOptions;
import net.sf.freecol.client.FreeColClient;


/**
 * An action for displaying the map controls.
 */
public class MapControlsAction extends SelectableAction {

    public static final String id = "mapControlsAction";


    /**
     * Creates this action.
     *
     * @param freeColClient The {@code FreeColClient} for the game.
     */
    public MapControlsAction(FreeColClient freeColClient) {
        super(freeColClient, id, ClientOptions.DISPLAY_MAP_CONTROLS);
    }


    // Override FreeColAction

    /**
     * {@inheritDoc}
     */
    @Override
    public void update() {
        super.update();

        getGUI().enableMapControls(isEnabled() && isSelected());
    }


    // Interface ActionListener

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent ae) {
        setSelected(((AbstractButton)ae.getSource()).isSelected());
        setOption(isSelected());
        update();
    }
}
