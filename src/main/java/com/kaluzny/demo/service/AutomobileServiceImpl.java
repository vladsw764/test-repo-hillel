package com.kaluzny.demo.service;

import com.kaluzny.demo.domain.Automobile;
import com.kaluzny.demo.domain.AutomobileRepository;
import com.kaluzny.demo.dto.AutoRequestDto;
import com.kaluzny.demo.dto.AutoResponseDto;
import com.kaluzny.demo.exception.ThereIsNoSuchAutoException;
import com.kaluzny.demo.mapper.AutomobileMapper;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Topic;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class AutomobileServiceImpl implements AutomobileService {

    private final AutomobileRepository automobileRepository;
    private final AutomobileMapper automobileMapper;
    private final JmsTemplate jmsTemplate;

    public AutomobileServiceImpl(AutomobileRepository automobileRepository, AutomobileMapper automobileMapper, JmsTemplate jmsTemplate) {
        this.automobileRepository = automobileRepository;
        this.automobileMapper = automobileMapper;
        this.jmsTemplate = jmsTemplate;
    }

    /**
     * Saves an automobile, pushes a message to a JMS topic, and returns the ID of the saved automobile.
     *
     * @param automobile The details of the automobile to be saved.
     * @return ResponseEntity containing the ID of the saved automobile, or INTERNAL_SERVER_ERROR if an error occurs.
     */
    @Override
    public ResponseEntity<UUID> saveAutomobileAndPushMessage(AutoRequestDto automobile) {
        try (Connection connection = Objects.requireNonNull(jmsTemplate.getConnectionFactory()).createConnection()) {
            Topic autoTopic = connection.createSession().createTopic("AutoTopic");
            Automobile savedAuto = automobileRepository.save(automobileMapper.mapToEntity(automobile));

            jmsTemplate.convertAndSend(autoTopic, savedAuto);

            return new ResponseEntity<>(savedAuto.getId(), HttpStatus.CREATED);
        } catch (JMSException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a collection of all existing automobiles.
     *
     * @return Collection of AutoResponseDto representing all existing automobiles.
     */
    @Override
    public Collection<AutoResponseDto> getAllAutomobiles() {
        return automobileRepository.findAllExists()
                .stream().map(automobileMapper::mapToDto)
                .toList();
    }

    /**
     * Retrieves an automobile by its unique ID.
     *
     * @param id The ID of the automobile to retrieve.
     * @return AutoResponseDto representing the retrieved automobile.
     * @throws ThereIsNoSuchAutoException if the automobile with the specified ID does not exist.
     */
    @Override
    public AutoResponseDto getAutomobileById(UUID id) {
        return automobileRepository.findById(id)
                .map(automobileMapper::mapToDto)
                .orElseThrow(ThereIsNoSuchAutoException::new);
    }

    /**
     * Retrieves a collection of automobiles by their name.
     *
     * @param name The name of the automobiles to retrieve.
     * @return Collection of AutoResponseDto representing automobiles with the specified name.
     */
    @Override
    public Collection<AutoResponseDto> findAutomobileByName(String name) {
        return automobileRepository.findByName(name)
                .stream().map(automobileMapper::mapToDto)
                .toList();
    }

    /**
     * Updates an existing automobile with new details and returns the updated automobile.
     *
     * @param id         The ID of the automobile to update.
     * @param automobile The updated details of the automobile.
     * @return AutoResponseDto representing the updated automobile.
     */
    @Override
    public AutoResponseDto refreshAutomobile(UUID id, AutoRequestDto automobile) {
        Automobile updatedAuto = automobileRepository.updateAutomobile(automobile.name(), automobile.color(), automobile.originalColor(), id);
        return automobileMapper.mapToDto(updatedAuto);
    }

    /**
     * Marks an automobile as removed based on its ID.
     *
     * @param id The ID of the automobile to remove.
     */
    @Override
    public void removeAutomobileById(UUID id) {
        automobileRepository.markRemoved(id);
    }

    /**
     * Removes all automobiles from the database.
     */
    @Override
    public void removeAllAutomobiles() {
        automobileRepository.deleteAll();
    }

    /**
     * Retrieves a collection of automobiles by their color and pushes the collection to a JMS topic.
     *
     * @param color The color of the automobiles to retrieve.
     * @return ResponseEntity containing the collection of retrieved automobiles, or INTERNAL_SERVER_ERROR if an error occurs.
     */
    @Override
    public ResponseEntity<Collection<AutoResponseDto>> findAutomobileByColor(String color) {
        try (Connection connection = Objects.requireNonNull(jmsTemplate.getConnectionFactory()).createConnection()) {
            Topic autoTopic = connection.createSession().createTopic("AutoTopicList");
            List<AutoResponseDto> automobiles = automobileRepository.findByColor(color)
                    .stream().map(automobileMapper::mapToDto)
                    .toList();

            jmsTemplate.convertAndSend(autoTopic, automobiles);

            return new ResponseEntity<>(automobiles, HttpStatus.OK);
        } catch (JMSException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves a collection of automobiles by their name and color.
     *
     * @param name  The name of the automobiles to retrieve.
     * @param color The color of the automobiles to retrieve.
     * @return Collection of AutoResponseDto representing automobiles with the specified name and color.
     */
    @Override
    public Collection<AutoResponseDto> findAutomobileByNameAndColor(String name, String color) {
        return automobileRepository.findByNameAndColor(name, color)
                .stream().map(automobileMapper::mapToDto)
                .toList();
    }

    /**
     * Retrieves a paged collection of automobiles by their color starting with a specified prefix.
     *
     * @param colorStartsWith The prefix for the color of the automobiles to retrieve.
     * @param page            The page number for pagination.
     * @param size            The number of items per page.
     * @return Collection of AutoResponseDto representing paged automobiles with colors starting with the specified prefix.
     */
    @Override
    public Collection<AutoResponseDto> findAutomobileByColorStartsWith(String colorStartsWith, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return automobileRepository.findByColorStartsWith(colorStartsWith, pageable)
                .stream().map(automobileMapper::mapToDto)
                .toList();
    }
}
