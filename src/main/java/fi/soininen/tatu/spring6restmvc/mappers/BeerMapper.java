package fi.soininen.tatu.spring6restmvc.mappers;

import fi.soininen.tatu.spring6restmvc.entities.Beer;
import fi.soininen.tatu.spring6restmvc.model.BeerDto;
import org.mapstruct.Mapper;

@Mapper
public interface BeerMapper {

    Beer beerDtoToBeer(BeerDto dto);
    BeerDto beerToBeerDto(Beer beer);
}
