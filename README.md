# Nationale Nederlanden - Zadanie Rekrutacyjne

Aplikacja do wymiany walut PLNL<->USD oraz USD<->PLN.

## Wymagania

Do uruchomienia aplikacji potrzebna jest Java 21. Do budowania wykorzystać należy Maven Wrapper. Baza danych oparta o H2 (zapis do pliku).

## Budowanie

```bash
$ ./mvnw clean package
```

## Uruchomienie
```bash
$ java -jar target/currency-exchange-0.0.1-SNAPSHOT.jar
```

#### REST API: [Swagger UI](http://localhost:8080/swagger-ui/index.html)
