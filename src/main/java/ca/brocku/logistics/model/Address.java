package ca.brocku.logistics.model;

/**
 * Address Record
 * Contains all information for location(s)
 *
 * @param streetName
 * @param primaryNumber
 * @param unit
 * @param postalCode
 * @param city
 * @param province
 * @param country
 */
public record Address(String streetName, int primaryNumber, int unit, String postalCode, String city, String province, String country) {
}
