package fi.soininen.tatu.spring6restmvc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.soininen.tatu.spring6restmvc.entities.Beer;
import fi.soininen.tatu.spring6restmvc.mappers.BeerMapper;
import fi.soininen.tatu.spring6restmvc.model.BeerDto;
import fi.soininen.tatu.spring6restmvc.model.BeerStyle;
import fi.soininen.tatu.spring6restmvc.repositories.BeerRepository;
import org.hamcrest.core.IsNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.core.Is.is;

@SpringBootTest
class BeerControllerIntegrationTest {

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerMapper beerMapper;


    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    WebApplicationContext wac;

    MockMvc mockMvc;

    // OAuth 2.0
    public static final SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtRequestPostProcessor =
        jwt().jwt(jwt -> {
                    jwt.claims(claims -> {
                            claims.put("scope", "message.read");
                            claims.put("scope", "message.write");
                    })
                    .subject("messaging-client")
                    .notBefore(Instant.now().minusSeconds(5));
        });
    // =========================================
    // HTTP Basic Authentication
    // =========================================
    // @Value("${spring.security.user.name}")
    // private String USERNAME;
    // -----------------------------------------
    // @Value("${spring.security.user.password}")
    // private String PASSWORD;
    // =========================================

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testUpdateNotFound() {
        UUID randomID = UUID.randomUUID();
        BeerDto empty = BeerDto.builder().build();

        assertThrows(NotFoundException.class, () -> {
            beerController.updateById(randomID, empty);
        });
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingBeer() {
        Beer beer = beerRepository.findAll().get(0);
        BeerDto dto = beerMapper.beerToBeerDto(beer);

        // Hibernate manages ID & version, thus cleared here
        dto.setId(null);
        dto.setVersion(null);

        final String beerName = "UPDATED";
        dto.setBeerName(beerName);

        ResponseEntity responseEntity = beerController.updateById(beer.getId(), dto);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);
    }

    @Test
    void testBeerIdNotFound() {
        UUID randomID = UUID.randomUUID();
        assertThrows(NotFoundException.class, () -> {
            beerController.getBeerById(randomID);
        });
    }

    @Test
    void testGetById() {
        Beer beer = beerRepository.findAll().get(0);

        BeerDto dto = beerController.getBeerById(beer.getId());

        assertThat(dto).isNotNull();
    }

    @Test
    void testListBeers() {
        Page<BeerDto> dtos = beerController.listBeers(null, null, false, 1, 25);

        assertThat(dtos).hasSize(25);
    }

    @Test
    void testListBeersByName() throws Exception {
        mockMvc.perform(
                    get(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .queryParam("beerName", "IPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)));
    }

    @Disabled
    @Test
    void testListBeersByNameWithBasicAuth() throws Exception {
        mockMvc.perform(
                    get(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .queryParam("beerName", "IPA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)));
    }


    @Test
    void testListBeersByStyle() throws Exception {
        mockMvc.perform(
                    get(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)));
    }

    @Test
    void testListBeersByNameAndStyle() throws Exception {
        mockMvc.perform(
                    get(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)));
    }


    @Test
    void testListBeersByNameAndStyleAndHideInventory() throws Exception {
        mockMvc.perform(
                    get(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "false")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.nullValue()));
    }

    @Test
    void testListBeersByNameAndStyleAndShowInventory() throws Exception {
        mockMvc.perform(
                    get(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "true")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(25)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Test
    void testListBeersByNameAndStyleAndShowInventoryPage2() throws Exception {
        mockMvc.perform(
                    get(BeerController.BEER_PATH)
                        .with(jwtRequestPostProcessor)
                        .queryParam("beerName", "IPA")
                        .queryParam("beerStyle", BeerStyle.IPA.name())
                        .queryParam("showInventory", "true")
                        .queryParam("pageNumber", "2")
                        .queryParam("pageSize", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()", is(50)))
                .andExpect(jsonPath("$.content.[0].quantityOnHand").value(IsNull.notNullValue()));
    }

    @Rollback
    @Transactional
    @Test
    void testEmptyList() {

        beerRepository.deleteAll();
        Page<BeerDto> dtos = beerController.listBeers(null, null, false, 1, 25);

        assertThat(dtos).isEmpty();
    }

    @Rollback
    @Transactional
    @Test
    void testSaveNewBeer() {
        BeerDto beerDto = BeerDto.builder()
                .beerName("New Beer")
                .build();

        ResponseEntity responseEntity = beerController.handlePost(beerDto);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        Beer beer = beerRepository.findById(savedUUID).get();
        assertThat(beer).isNotNull();
    }

    @Rollback
    @Transactional
    @Test
    void deleteByIdFound() {
        Beer beer = beerRepository.findAll().get(0);

        ResponseEntity responseEntity = beerController.deleteById(beer.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));
        assertThat(beerRepository.findById(beer.getId()).isEmpty());
    }

    @Test
    void deleteByIdNotFound() {
        UUID randomID = UUID.randomUUID();

        assertThrows(NotFoundException.class, () -> {
            beerController.deleteById(randomID);
        });
    }

    // This test was to showcase optimistic locking with JPA
    @Disabled
    @Test
    void testUpdateBeerBadVersion() throws Exception {
        Beer beer = beerRepository.findAll().get(0);
        BeerDto dto = beerMapper.beerToBeerDto(beer);
        dto.setBeerName("Updated name");

        MvcResult result = mockMvc.perform(
                                        put(BeerController.BEER_PATH_ID, beer.getId())
                                                .with(jwtRequestPostProcessor)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .accept(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(dto)))
                                    .andExpect(status().isNoContent())
                                    .andReturn();

        System.out.println(result.getResponse().getContentAsString());

        dto.setBeerName("Updated name #2");

        MvcResult result2 = mockMvc.perform(
                                        put(BeerController.BEER_PATH_ID, beer.getId())
                                            .with(jwtRequestPostProcessor)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .accept(MediaType.APPLICATION_JSON)
                                            .content(objectMapper.writeValueAsString(dto)))
                            .andExpect(status().isNoContent())
                            .andReturn();

        System.out.println(result2.getResponse().getContentAsString());
    }
}