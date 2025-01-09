package agh.ics.poproject.simulation;

import agh.ics.poproject.model.elements.Animal;
import agh.ics.poproject.model.elements.Plant;
import agh.ics.poproject.model.map.GlobeMap;
import agh.ics.poproject.model.map.IncorrectPositionException;
import agh.ics.poproject.model.map.WorldMap;
import agh.ics.poproject.util.Configuration;

import java.util.ArrayList;
import java.util.List;

public class Simulation implements Runnable {
    //to tak jakby nasz engine do backendu, tu powinny być wszystkie listy zwierząt itd
    private final Configuration config;


    private GlobeMap worldMap;  // można pomyśleć czy tu nie stworzyć GlobeMap a nie w SImulationPresenter?
    private ArrayList<Animal> animals;
    private ArrayList<Plant> plants;

    public Simulation(Configuration config) {
        this.config = config;

        if (config.isGlobeMap()) {
            this.setWorldMap(new GlobeMap(config.mapWidth(), config.mapHeight()));
        }
        this.animals = new ArrayList<>();
        this.plants = new ArrayList<>();
    }

    public List<Animal> getAnimals() {
        return animals;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public GlobeMap getWorldMap() {
        return worldMap;
    }

    public void setWorldMap(GlobeMap worldMap) {
        this.worldMap = worldMap;
    }

    public Configuration getConfig() {
        return config;
    }

    // TODO: osbługa errorów
    @Override
    public void run()  {
        System.out.println("Simulation started");
        Day simulationDay = new Day(this);
        try {
            simulationDay.firstDayActivities();
            System.out.println(simulationDay.getDayCount());
        } catch (IncorrectPositionException e) {
            throw new RuntimeException(e);
        }

        while (!animals.isEmpty()) {
            try {
                simulationDay.everyDayActivities();
                System.out.println(simulationDay.getDayCount());
                System.out.println("_________");

                // Wait for 1 second
                Thread.sleep(1000);
            } catch (IncorrectPositionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                throw new RuntimeException("Thread was interrupted", e);
            }
        }
    }

    public void addAnimal(Animal animal) {
        animals.add(animal);
    }

    public void addPlant(Plant plant) {
        plants.add(plant);
    }


}
