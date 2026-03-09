package com.classicjazz.service;

import com.classicjazz.model.Album;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatalogService {

    /**
     * Static catalog of 6 Classic Jazz albums with Wikipedia/Wikimedia Commons image URLs.
     * Album covers used where available; artist photos from Wikimedia Commons otherwise.
     */
    private static final List<Album> ALBUMS = List.of(
            new Album(
                    "CJ-GS-001",
                    "John Coltrane",
                    "Giant Steps",
                    29.99,
                    "https://upload.wikimedia.org/wikipedia/commons/2/2a/Coltrane_Giant_Steps.jpg"
            ),
            new Album(
                    "CJ-MA-002",
                    "Miles Davis",
                    "Amandla",
                    24.99,
                    "https://upload.wikimedia.org/wikipedia/en/f/fa/Amandla_%28album%29.jpg"
            ),
            new Album(
                    "CJ-IO-003",
                    "Joe Henderson",
                    "In and Out",
                    27.99,
                    "https://upload.wikimedia.org/wikipedia/commons/5/5f/Joe_Henderson_2.jpg"
            ),
            new Album(
                    "CJ-ND-004",
                    "Wayne Shorter",
                    "Night Dreamer",
                    26.99,
                    "https://upload.wikimedia.org/wikipedia/en/0/04/Night_Dreamer.jpg"
            ),
            new Album(
                    "CJ-LM-005",
                    "Hermeto Paschoal",
                    "Live at Montreux",
                    22.99,
                    "https://upload.wikimedia.org/wikipedia/commons/a/af/03_230624_-_Bruno_Figueiredo_-_SENSA_-_082_%2853045415791%29.jpg"
            ),
            new Album(
                    "CJ-WO-006",
                    "Ray Brown",
                    "Walk On",
                    25.99,
                    "https://upload.wikimedia.org/wikipedia/commons/9/98/Ray_Brown.jpg"
            )
    );

    public List<Album> getAllAlbums() {
        return ALBUMS;
    }

    public Album findBySku(String sku) {
        return ALBUMS.stream()
                .filter(a -> a.sku().equals(sku))
                .findFirst()
                .orElse(null);
    }
}
