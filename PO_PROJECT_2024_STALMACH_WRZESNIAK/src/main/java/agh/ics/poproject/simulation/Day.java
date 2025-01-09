package agh.ics.poproject.simulation;

import agh.ics.poproject.inheritance.Genome;
import agh.ics.poproject.inheritance.Reproduce;
import agh.ics.poproject.model.Vector2d;
import agh.ics.poproject.model.elements.Animal;
import agh.ics.poproject.model.elements.Plant;
import agh.ics.poproject.model.map.GlobeMap;
import agh.ics.poproject.model.map.IncorrectPositionException;
import agh.ics.poproject.util.Configuration;

import java.util.*;
// TODO: aktualizować wszystko na mapie
// TODO: osługa błędów w momencie gdzie nie ma już na mapie miejsca dla nowych zwierzaków bądź nowych roślin
/**
 * Class for handling activities for each day for each simulation.
 */
public class Day {
    private final Simulation simulation;
    private final Configuration config;
    private int dayCount = 0;

    Map<Vector2d, List<Animal>> positionMap = new HashMap<>(); //dict key: position vector, val: animal

    private GlobeMap worldMap;

    public Day(Simulation simulation) {
        this.simulation = simulation;
        this.config = simulation.getConfig();
        this.worldMap = simulation.getWorldMap();
    }

    /**
    Generates all necessary elements for simulation launch
     */
    void firstDayActivities() throws IncorrectPositionException {
        dayCount++;
        worldMap.generateAnimals(simulation);
        worldMap.generatePlants(simulation);
    }

    /**
     Generates and updated all map elements in the timeframe of one day
     */
     void everyDayActivities() throws IncorrectPositionException {
        dayCount++;
        ageAllAnimals(); //adds +1 to each animal's age
        removeDeadAnimals();
        moveAndRotateAnimals();
        consumePlants();
        reproduceAnimals();
        growNewPlants();
        //TODO: można pomyśleć nad jakimś counterem dni
    }

    public int getDayCount() {
        return dayCount;
    }



    /**
     * Removes dead animals from animals list in Simulation class.
     */
    private void ageAllAnimals() {
        for (Animal animal : simulation.getAnimals()) {
            animal.ageAnimal();
        }
    }

    /**
     * Removes dead animals from the map
     */
    private void removeDeadAnimals() {
        List<Animal> animals = simulation.getAnimals();
        for (Animal animal : animals) {
            if (animal.isDead()) {
                simulation.getWorldMap().removeElement(animal, animal.getPosition());  // from map
                simulation.getAnimals().remove(animal);  // from simulation
            }
        }
    }

    private void moveAndRotateAnimals() {
        List<Animal> animals = simulation.getAnimals();
        for (Animal animal : animals) {
            simulation.getWorldMap().move(animal);
        }
    }

    private void growNewPlants() {
        int numberOfPlants = config.dailyPlantGrowth();
        // TODO:wywołanie metody growPlants z worldMap -> implementacja  jej
    }

    /**
     * Groups animals by key: position
     */
    private void groupAnimalsByPositions() {
        for (Animal animal : simulation.getAnimals()) {
            List<Animal> animalPositions = positionMap.computeIfAbsent(animal.getPosition(), k -> new ArrayList<>());
            animalPositions.add(animal);
        }
    }

    /**
     * Establishes the animals that will reproduce, resolves conflicts in case of multiple animals on a position.
     * Handles the simulation update post reproduction
     *
     */
    private void reproduceAnimals() throws IncorrectPositionException {
        groupAnimalsByPositions();

        for (List<Animal> animals : positionMap.values()) {
            List<Animal> priorityForReproduction = animals.stream()
                    .filter(animal -> animal.getRemainingEnergy() > config.neededEnergyForReproduction()) //only those with sufficient energy
                    .sorted((animal1, animal2) -> { //sort for reproduction priority
                        int energyComparison = Integer.compare(animal2.getRemainingEnergy(), animal1.getRemainingEnergy());
                        if (energyComparison != 0) {
                            return energyComparison;
                        }
                        return Integer.compare(animal2.getAge(), animal1.getAge());
                    }).toList();

            if (priorityForReproduction.size() >= 2) {
                Animal animal1 = priorityForReproduction.get(0);
                Animal animal2 = priorityForReproduction.get(1);

                Reproduce reproduction = new Reproduce();
                Animal babyAnimal = reproduction.reproduce(animal1, animal2);
                simulation.addAnimal(babyAnimal);
                simulation.getWorldMap().placeWorldElement(babyAnimal);
            }
        }
    }


    /**
     * Establishes the animal that will consume the Plant, resolves conflicts in case of multiple animals on a position.
     * Handles the simulation update post plant consumption
     */
    private void consumePlants() {
        List<Plant> plants = simulation.getPlants();

        groupAnimalsByPositions();

        for (List<Animal> animals : positionMap.values()) {
            List<Animal> priorityForFood = animals.stream()
                    .sorted((animal1, animal2) -> {
                        int energyComparison = Integer.compare(animal2.getRemainingEnergy(), animal1.getRemainingEnergy());
                        if (energyComparison != 0) {
                            return energyComparison;
                        }
                        return Integer.compare(animal2.getAge(), animal1.getAge());
                    }).toList();
            Iterator<Plant> iterator = plants.iterator();
            while (iterator.hasNext()) {
                Plant plant = iterator.next();
                Vector2d plantPosition = plant.getPosition();

                if (positionMap.containsKey(plantPosition)) {
                    List<Animal> animalsPositions = positionMap.get(plantPosition);

                    if (!animalsPositions.isEmpty()) {
                        Animal animal = priorityForFood.getFirst();
                        animal.eat(config.energyPerPlant());
                        iterator.remove();
                        simulation.getWorldMap().removeElement(plant, plant.getPosition());
                    }
                }
            }
        }


    }
}
