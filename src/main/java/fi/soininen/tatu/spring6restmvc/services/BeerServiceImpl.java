package fi.soininen.tatu.spring6restmvc.services;

import fi.soininen.tatu.spring6restmvc.model.BeerDto;
import fi.soininen.tatu.spring6restmvc.model.BeerStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by jt, Spring Framework Guru.
 */
@Slf4j
@Service
public class BeerServiceImpl implements BeerService {

    private Map<UUID, BeerDto> beerMap;

    public BeerServiceImpl() {
        this.beerMap = new HashMap<>();

        BeerDto beer1 = BeerDto.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Galaxy Cat")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("12356")
                .price(new BigDecimal("12.99"))
                .quantityOnHand(122)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        BeerDto beer2 = BeerDto.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Crank")
                .beerStyle(BeerStyle.PALE_ALE)
                .upc("12356222")
                .price(new BigDecimal("11.99"))
                .quantityOnHand(392)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        BeerDto beer3 = BeerDto.builder()
                .id(UUID.randomUUID())
                .version(1)
                .beerName("Sunshine City")
                .beerStyle(BeerStyle.IPA)
                .upc("12356")
                .price(new BigDecimal("13.99"))
                .quantityOnHand(144)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .build();

        beerMap.put(beer1.getId(), beer1);
        beerMap.put(beer2.getId(), beer2);
        beerMap.put(beer3.getId(), beer3);
    }

    @Override
    public Page<BeerDto> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize){
        return new PageImpl<>(new ArrayList<>(beerMap.values()));
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID id) {

        log.debug("Get Beer by Id - in service. Id: " + id.toString());

        return Optional.of(beerMap.get(id));
    }

    @Override
    public BeerDto saveNewBeer(BeerDto beer) {

        BeerDto savedBeer = BeerDto.builder()
                .id(UUID.randomUUID())
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .beerName(beer.getBeerName())
                .beerStyle(beer.getBeerStyle())
                .quantityOnHand(beer.getQuantityOnHand())
                .upc(beer.getUpc())
                .build();

        log.info("Saving new beer: {} ({})", beer.getBeerName(), savedBeer.getId());

        beerMap.put(savedBeer.getId(), savedBeer);

        return savedBeer;
    }

    @Override
    public Optional<BeerDto> updateBeerById(UUID beerId, BeerDto beer) {
        BeerDto existing = beerMap.get(beerId);

        // Change the value only if PUT payload has new information
        // Otherwise, preserve the original value in all fields

        if (beer.getBeerName() != null) {
            existing.setBeerName(beer.getBeerName());
        }

        if (beer.getPrice() != null) {
            existing.setPrice(beer.getPrice());
        }

        if (beer.getUpc() != null) {
            existing.setUpc(beer.getUpc());
        }

        if (beer.getQuantityOnHand() != null) {
            existing.setQuantityOnHand(beer.getQuantityOnHand());
        }

        if (beer.getBeerStyle() != null) {
            existing.setBeerStyle(beer.getBeerStyle());
        }

        // Always refresh automatically calculated values
        existing.setVersion(existing.getVersion() + 1);
        existing.setUpdatedDate(LocalDateTime.now());

        return Optional.of(existing);
    }

    @Override
    public Boolean deleteById(UUID beerId) {
        beerMap.remove(beerId);
        return true;
    }
}
