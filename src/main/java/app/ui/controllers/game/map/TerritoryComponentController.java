package app.ui.controllers.game.map;

import app.domain.models.GameMap.Territory;
import app.ui.views.game.map.TerritoryComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class TerritoryComponentController {
    private TerritoryComponent territoryComponent;
    private Territory territory;

    public TerritoryComponentController(Territory territory) throws IOException {
        this.territory = territory;
        this.territoryComponent = new TerritoryComponent(territory.name, territory.getImage());
        setupListeners();
    }

    private void setupListeners() {
        territoryComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleTerritoryClicked();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                handleTerritoryEnter();
            }
        });
    }

    public TerritoryComponent getTerritoryComponent() {
        return territoryComponent;
    }

    public void updateComponentBounds() {
        territoryComponent.setBounds(territory.territoryPosition.x,
                territory.territoryPosition.y,
                territoryComponent.getPreferredSize().width,
                territoryComponent.getPreferredSize().height);
    }

    private void handleTerritoryClicked() {
        System.out.println("Territory clicked: " + territory.name);
    }

    private void handleTerritoryEnter() {
        System.out.println("Territory hover: " + territory.name);
    }
}