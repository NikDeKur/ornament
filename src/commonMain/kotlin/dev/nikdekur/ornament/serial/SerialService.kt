/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * Copyright (c) 2024-present "Nik De Kur"
 */

package dev.nikdekur.ornament.serial

/**
 * # Serial (Serializing) Service
 *
 * Service for serializing and deserializing NexusData objects.
 *
 */
public interface SerialService {

    /**
     * Serializes the given NexusData to a string.
     *
     * The string should be able to be deserialized back into the NexusData object.
     *
     * @param data The NexusData object to serialize.
     * @return The serialized string.
     */
    public fun serialize(data: MapData): String

    /**
     * Deserializes the given string into a NexusData object.
     *
     * The string should have been serialized from a NexusData object.
     *
     * @param data The serialized string.
     * @return The deserialized NexusData object.
     */
    public fun deserialize(data: String): MapData
}

public typealias MapData = Map<String, *>