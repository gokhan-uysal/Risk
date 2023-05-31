package app.domain.services;

import app.common.Logger;
import app.common.errors.DbException;
import app.domain.models.card.*;
import app.domain.models.card.army.ArmyCardType;
import app.domain.models.game.GameState;
import app.domain.models.game.map.Territory;
import app.domain.models.player.Player;
import app.domain.services.base.BasePublisher;
import app.domain.services.map.MapService;
import app.domain.services.states.AttackState;
import app.domain.services.states.BuildState;
import app.domain.services.states.CardTradeState;
import app.domain.services.states.DistributeState;
import app.domain.services.states.FortifyState;
import app.domain.services.states.RecieveState;
import app.domain.services.states.ReplaceState;

import java.io.IOException;
import java.util.*;

public class GameManagerService extends BasePublisher<GameState> {
    private static GameManagerService _instance;

    private BuildState _buildState;
    private DistributeState _distributeState;
    private RecieveState _recieveState;
    private ReplaceState _replaceState;
    private CardTradeState _cardTradeState;
    private AttackState _attackState;
    private FortifyState _fortifyState;

    private MapService _mapService;
    private MainDecks _centralDeck;
    private PlayerService _playerService;
    private List<Player> _players;
    private int _currentPlayer;
    private boolean _isGameFinished;

    private GameManagerService() {
        super(GameState.BUILDING_STATE);
        initDependicies();
        initializeGame();
    }

    public static GameManagerService getInstance() {
        if (_instance == null) {
            _instance = new GameManagerService();
        }
        return _instance;
    }

    private void initDependicies() {
        _buildState = new BuildState();
        _distributeState = new DistributeState();
        _recieveState = new RecieveState();
        _replaceState = new ReplaceState();
        _cardTradeState = new CardTradeState();
        _attackState = new AttackState();
        _fortifyState = new FortifyState();
        _mapService = MapService.getInstance();
        _centralDeck = new MainDecks();
        _playerService = PlayerService.getInstance();
    }

    private void initializeGame() {
        updateGameState(getState());
        _isGameFinished = false;
    }

    private void updateGameState(GameState newState) {
        setState(newState);
        notifySubscribers();
    }

    public void handleNextState() {
        GameState currentState = super.getState();
        switch (currentState) {
            case LOADING_STATE:
                updateGameState(GameState.BUILDING_STATE);
                break;
            case BUILDING_STATE:
                initilizeArmyDeck(_playerService.getPlayerCount());
                initilizeTerritoyDeck(_mapService.getTerritoryListFromGraph());
                updateGameState(GameState.DISTRIBUTING_STATE);
                initilizeArmyUnits(PlayerService.getInstance().getPlayerCount());
                break;
            case DISTRIBUTING_STATE:
                if (_distributeState.isInitialUnitFinished()) {
                    updateGameState(GameState.REPLACEMENT_STATE);
                    _playerService.resatrtTurn();
                    break;
                }
                _playerService.turnChange();
                updateGameState(GameState.DISTRIBUTING_STATE);
                break;
            case RECEIVING_STATE:
                updateGameState(GameState.REPLACEMENT_STATE);
                break;
            case REPLACEMENT_STATE:
                updateGameState(GameState.TRADE_CARD_STATE);
                break;
            case TRADE_CARD_STATE:
                updateGameState(GameState.ATTACK_STATE);
                break;
            case ATTACK_STATE:
                updateGameState(GameState.RECEIVING_STATE);
                break;
            case FORTIFY_STATE:
                if (_isGameFinished) {
                    updateGameState(GameState.GAME_OVER_STATE);
                    break;
                }
                _playerService.turnChange();
                updateGameState(GameState.RECEIVING_STATE);
                break;
            default:
                break;
        }
    }

    public void createPlayers(ArrayList<String> names) {
        _playerService.createPlayers(names);
    }

    public void loadMap() {
        try {
            _mapService.loadGameMapDataToGraph();
        } catch (DbException e) {
            Logger.error(e);
        }
        setState(GameState.BUILDING_STATE);
        notifySubscribers();
    }

    public List<Territory> getMap() {
        return _mapService.getTerritoryListFromGraph();
    }

    public MainDecks getCentralDeck() {
        return this._centralDeck;
    }

    public boolean validateNewBuildMap() {
        if (!_mapService.isValidBuildSelection()) {
            return false;
        }
        _mapService.removeClosedTerritories();
        return true;
    }

    public void initilizeArmyDeck(int playerCount) {
        _centralDeck.addArmyCards(ArmyCardType.Infantry, playerCount * 3);
        _centralDeck.addArmyCards(ArmyCardType.Cavalry, playerCount * 2);
        _centralDeck.addArmyCards(ArmyCardType.Artillery, playerCount);
        _centralDeck.shuffle();
    }

    public void initilizeTerritoyDeck(List<Territory> territoryList) {
        for (Territory territory : territoryList) {
            try {
                _centralDeck.addTerritoryCards(territory.getName(), territory.getImage(), territory.get_territoryId());
            } catch (IOException e) {
                Logger.error(e);
            }
            _centralDeck.shuffle();
        }
    }

    public void initilizeArmyUnits(int playerCount) {
        int unitAmount = (45 - (playerCount - 1) * 5) * playerCount;
        _distributeState.fillArmy(unitAmount);
    }

    public void placeInfantryToTerritory(Territory territory, Player player) {
        _distributeState.placeInfantryToTerritory(territory, player.getId());
        handleNextState();
    }

    public void receiveUnits(int territoryId) {
        _recieveState.placeRecievedUnits(territoryId);
        handleNextState();
    }

    public int receivedUnitNumber() {
        return _recieveState.receivedUnitAmount();
    }

    public String attack(int attackTerritoryId, int defenderTerritoryId) {
        Player player = _playerService.getCurrentPlayer();

        if (_centralDeck.isEmpty()) {
            _playerService.emptyPlayerDecks();
            this.initilizeArmyDeck(_playerService.getPlayerCount());
            this.initilizeTerritoyDeck(_mapService.getTerritoryListFromGraph());
        }

        _attackState.attack(player.getId(), attackTerritoryId, defenderTerritoryId);
        return _attackState.getWinningPlayer();
    }

    public void tradeArmyCards(int iAmount, int cAmount, int aAmount, int territoryId) {
        _cardTradeState.tradeArmyCards(iAmount, cAmount, aAmount, _playerService.getCurrentPlayer().getId(),
                territoryId);
    }

    public void tradeTerritoryCards(String continentName) {
        _cardTradeState.tradeTerritoryCards(continentName, _playerService.getCurrentPlayer().getId());
    }

    public void tradeArmyUnits(int infantryAmount, int cavalryAmount, int territoryId) {
        _replaceState.replaceUnits(infantryAmount, cavalryAmount, territoryId);
    }

    public void fortify(int infantryAmount, int cavalryAmount, int artilleryAmount,
            int startTerritoryId, int destinationTerritoryId, int playerId) {
        _fortifyState.fortify(infantryAmount, cavalryAmount, artilleryAmount, startTerritoryId,
                destinationTerritoryId, playerId);
    }
}