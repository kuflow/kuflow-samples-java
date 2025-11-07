/*
 * The MIT License
 * Copyright © 2021-present KuFlow S.L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.kuflow.samples.temporal.worker.loan.model;

import java.util.List;
import java.util.Map;

/**
 * Mock product data for testing data source pagination.
 */
public final class DataSourceMocks {

    private DataSourceMocks() {
        // Utility class
    }

    public static final List<Map<String, Object>> MOCK_PRODUCTS = List.<Map<String, Object>>of(
        Map.of("id", "prod-001", "label", "Widget A", "name", "Widget A", "sku", "WDG-A-001", "price", 99.99, "stock", 150),
        Map.of("id", "prod-002", "label", "Widget B", "name", "Widget B", "sku", "WDG-B-002", "price", 149.99, "stock", 75),
        Map.of("id", "prod-003", "label", "Widget C", "name", "Widget C", "sku", "WDG-C-003", "price", 199.99, "stock", 120),
        Map.of("id", "prod-004", "label", "Widget D", "name", "Widget D", "sku", "WDG-D-004", "price", 89.99, "stock", 200),
        Map.of("id", "prod-005", "label", "Widget E", "name", "Widget E", "sku", "WDG-E-005", "price", 129.99, "stock", 95),
        Map.of("id", "prod-006", "label", "Widget F", "name", "Widget F", "sku", "WDG-F-006", "price", 159.99, "stock", 110),
        Map.of("id", "prod-007", "label", "Widget G", "name", "Widget G", "sku", "WDG-G-007", "price", 179.99, "stock", 85),
        Map.of("id", "prod-008", "label", "Widget H", "name", "Widget H", "sku", "WDG-H-008", "price", 139.99, "stock", 140),
        Map.of("id", "prod-009", "label", "Widget I", "name", "Widget I", "sku", "WDG-I-009", "price", 109.99, "stock", 165),
        Map.of("id", "prod-010", "label", "Widget J", "name", "Widget J", "sku", "WDG-J-010", "price", 119.99, "stock", 180),
        Map.of("id", "prod-011", "label", "Widget K", "name", "Widget K", "sku", "WDG-K-011", "price", 94.99, "stock", 155),
        Map.of("id", "prod-012", "label", "Widget L", "name", "Widget L", "sku", "WDG-L-012", "price", 104.99, "stock", 125),
        Map.of("id", "prod-013", "label", "Widget M", "name", "Widget M", "sku", "WDG-M-013", "price", 114.99, "stock", 145),
        Map.of("id", "prod-014", "label", "Widget N", "name", "Widget N", "sku", "WDG-N-014", "price", 124.99, "stock", 135),
        Map.of("id", "prod-015", "label", "Widget O", "name", "Widget O", "sku", "WDG-O-015", "price", 134.99, "stock", 115),
        Map.of("id", "prod-016", "label", "Widget P", "name", "Widget P", "sku", "WDG-P-016", "price", 144.99, "stock", 105),
        Map.of("id", "prod-017", "label", "Widget Q", "name", "Widget Q", "sku", "WDG-Q-017", "price", 154.99, "stock", 95),
        Map.of("id", "prod-018", "label", "Widget R", "name", "Widget R", "sku", "WDG-R-018", "price", 164.99, "stock", 85),
        Map.of("id", "prod-019", "label", "Widget S", "name", "Widget S", "sku", "WDG-S-019", "price", 174.99, "stock", 175),
        Map.of("id", "prod-020", "label", "Widget T", "name", "Widget T", "sku", "WDG-T-020", "price", 184.99, "stock", 165),
        Map.of("id", "prod-021", "label", "Widget U", "name", "Widget U", "sku", "WDG-U-021", "price", 194.99, "stock", 155),
        Map.of("id", "prod-022", "label", "Widget V", "name", "Widget V", "sku", "WDG-V-022", "price", 79.99, "stock", 145),
        Map.of("id", "prod-023", "label", "Widget W", "name", "Widget W", "sku", "WDG-W-023", "price", 69.99, "stock", 135),
        Map.of("id", "prod-024", "label", "Widget X", "name", "Widget X", "sku", "WDG-X-024", "price", 59.99, "stock", 125),
        Map.of("id", "prod-025", "label", "Widget Y", "name", "Widget Y", "sku", "WDG-Y-025", "price", 49.99, "stock", 115),
        Map.of("id", "prod-026", "label", "Widget Z", "name", "Widget Z", "sku", "WDG-Z-026", "price", 209.99, "stock", 105),
        Map.of("id", "prod-027", "label", "Widget AA", "name", "Widget AA", "sku", "WDG-AA-027", "price", 219.99, "stock", 95),
        Map.of("id", "prod-028", "label", "Widget AB", "name", "Widget AB", "sku", "WDG-AB-028", "price", 229.99, "stock", 185),
        Map.of("id", "prod-029", "label", "Widget AC", "name", "Widget AC", "sku", "WDG-AC-029", "price", 239.99, "stock", 175),
        Map.of("id", "prod-030", "label", "Widget AD", "name", "Widget AD", "sku", "WDG-AD-030", "price", 249.99, "stock", 165),
        Map.of("id", "prod-031", "label", "Widget AE", "name", "Widget AE", "sku", "WDG-AE-031", "price", 259.99, "stock", 155),
        Map.of("id", "prod-032", "label", "Widget AF", "name", "Widget AF", "sku", "WDG-AF-032", "price", 269.99, "stock", 145),
        Map.of("id", "prod-033", "label", "Widget AG", "name", "Widget AG", "sku", "WDG-AG-033", "price", 279.99, "stock", 135),
        Map.of("id", "prod-034", "label", "Widget AH", "name", "Widget AH", "sku", "WDG-AH-034", "price", 289.99, "stock", 125),
        Map.of("id", "prod-035", "label", "Widget AI", "name", "Widget AI", "sku", "WDG-AI-035", "price", 299.99, "stock", 115),
        Map.of("id", "prod-036", "label", "Widget AJ", "name", "Widget AJ", "sku", "WDG-AJ-036", "price", 84.99, "stock", 190),
        Map.of("id", "prod-037", "label", "Widget AK", "name", "Widget AK", "sku", "WDG-AK-037", "price", 74.99, "stock", 170),
        Map.of("id", "prod-038", "label", "Widget AL", "name", "Widget AL", "sku", "WDG-AL-038", "price", 64.99, "stock", 160),
        Map.of("id", "prod-039", "label", "Widget AM", "name", "Widget AM", "sku", "WDG-AM-039", "price", 54.99, "stock", 150),
        Map.of("id", "prod-040", "label", "Widget AN", "name", "Widget AN", "sku", "WDG-AN-040", "price", 44.99, "stock", 140),
        Map.of("id", "prod-041", "label", "Widget AO", "name", "Widget AO", "sku", "WDG-AO-041", "price", 189.99, "stock", 130),
        Map.of("id", "prod-042", "label", "Widget AP", "name", "Widget AP", "sku", "WDG-AP-042", "price", 169.99, "stock", 120),
        Map.of("id", "prod-043", "label", "Widget AQ", "name", "Widget AQ", "sku", "WDG-AQ-043", "price", 159.99, "stock", 110),
        Map.of("id", "prod-044", "label", "Widget AR", "name", "Widget AR", "sku", "WDG-AR-044", "price", 149.99, "stock", 100),
        Map.of("id", "prod-045", "label", "Widget AS", "name", "Widget AS", "sku", "WDG-AS-045", "price", 139.99, "stock", 90),
        Map.of("id", "prod-046", "label", "Widget AT", "name", "Widget AT", "sku", "WDG-AT-046", "price", 129.99, "stock", 195),
        Map.of("id", "prod-047", "label", "Widget AU", "name", "Widget AU", "sku", "WDG-AU-047", "price", 119.99, "stock", 185),
        Map.of("id", "prod-048", "label", "Widget AV", "name", "Widget AV", "sku", "WDG-AV-048", "price", 109.99, "stock", 175),
        Map.of("id", "prod-049", "label", "Widget AW", "name", "Widget AW", "sku", "WDG-AW-049", "price", 99.99, "stock", 165),
        Map.of("id", "prod-050", "label", "Widget AX", "name", "Widget AX", "sku", "WDG-AX-050", "price", 89.99, "stock", 155),
        Map.of("id", "prod-051", "label", "Gadget A", "name", "Gadget A", "sku", "GDG-A-051", "price", 299.99, "stock", 80),
        Map.of("id", "prod-052", "label", "Gadget B", "name", "Gadget B", "sku", "GDG-B-052", "price", 349.99, "stock", 65),
        Map.of("id", "prod-053", "label", "Gadget C", "name", "Gadget C", "sku", "GDG-C-053", "price", 399.99, "stock", 90),
        Map.of("id", "prod-054", "label", "Gadget D", "name", "Gadget D", "sku", "GDG-D-054", "price", 449.99, "stock", 55),
        Map.of("id", "prod-055", "label", "Gadget E", "name", "Gadget E", "sku", "GDG-E-055", "price", 499.99, "stock", 70),
        Map.of("id", "prod-056", "label", "Gadget F", "name", "Gadget F", "sku", "GDG-F-056", "price", 549.99, "stock", 45),
        Map.of("id", "prod-057", "label", "Gadget G", "name", "Gadget G", "sku", "GDG-G-057", "price", 599.99, "stock", 60),
        Map.of("id", "prod-058", "label", "Gadget H", "name", "Gadget H", "sku", "GDG-H-058", "price", 649.99, "stock", 35),
        Map.of("id", "prod-059", "label", "Gadget I", "name", "Gadget I", "sku", "GDG-I-059", "price", 699.99, "stock", 50),
        Map.of("id", "prod-060", "label", "Gadget J", "name", "Gadget J", "sku", "GDG-J-060", "price", 749.99, "stock", 25),
        Map.of("id", "prod-061", "label", "Gadget K", "name", "Gadget K", "sku", "GDG-K-061", "price", 799.99, "stock", 40),
        Map.of("id", "prod-062", "label", "Gadget L", "name", "Gadget L", "sku", "GDG-L-062", "price", 849.99, "stock", 30),
        Map.of("id", "prod-063", "label", "Gadget M", "name", "Gadget M", "sku", "GDG-M-063", "price", 899.99, "stock", 20),
        Map.of("id", "prod-064", "label", "Gadget N", "name", "Gadget N", "sku", "GDG-N-064", "price", 949.99, "stock", 15),
        Map.of("id", "prod-065", "label", "Gadget O", "name", "Gadget O", "sku", "GDG-O-065", "price", 999.99, "stock", 10),
        Map.of("id", "prod-066", "label", "Gadget P", "name", "Gadget P", "sku", "GDG-P-066", "price", 319.99, "stock", 85),
        Map.of("id", "prod-067", "label", "Gadget Q", "name", "Gadget Q", "sku", "GDG-Q-067", "price", 369.99, "stock", 75),
        Map.of("id", "prod-068", "label", "Gadget R", "name", "Gadget R", "sku", "GDG-R-068", "price", 419.99, "stock", 95),
        Map.of("id", "prod-069", "label", "Gadget S", "name", "Gadget S", "sku", "GDG-S-069", "price", 469.99, "stock", 100),
        Map.of("id", "prod-070", "label", "Gadget T", "name", "Gadget T", "sku", "GDG-T-070", "price", 519.99, "stock", 65),
        Map.of("id", "prod-071", "label", "Tool A", "name", "Tool A", "sku", "TL-A-071", "price", 39.99, "stock", 250),
        Map.of("id", "prod-072", "label", "Tool B", "name", "Tool B", "sku", "TL-B-072", "price", 49.99, "stock", 230),
        Map.of("id", "prod-073", "label", "Tool C", "name", "Tool C", "sku", "TL-C-073", "price", 59.99, "stock", 210),
        Map.of("id", "prod-074", "label", "Tool D", "name", "Tool D", "sku", "TL-D-074", "price", 69.99, "stock", 190),
        Map.of("id", "prod-075", "label", "Tool E", "name", "Tool E", "sku", "TL-E-075", "price", 79.99, "stock", 270),
        Map.of("id", "prod-076", "label", "Tool F", "name", "Tool F", "sku", "TL-F-076", "price", 89.99, "stock", 240),
        Map.of("id", "prod-077", "label", "Tool G", "name", "Tool G", "sku", "TL-G-077", "price", 99.99, "stock", 220),
        Map.of("id", "prod-078", "label", "Tool H", "name", "Tool H", "sku", "TL-H-078", "price", 109.99, "stock", 200),
        Map.of("id", "prod-079", "label", "Tool I", "name", "Tool I", "sku", "TL-I-079", "price", 119.99, "stock", 180),
        Map.of("id", "prod-080", "label", "Tool J", "name", "Tool J", "sku", "TL-J-080", "price", 129.99, "stock", 260),
        Map.of("id", "prod-081", "label", "Tool K", "name", "Tool K", "sku", "TL-K-081", "price", 34.99, "stock", 290),
        Map.of("id", "prod-082", "label", "Tool L", "name", "Tool L", "sku", "TL-L-082", "price", 44.99, "stock", 280),
        Map.of("id", "prod-083", "label", "Tool M", "name", "Tool M", "sku", "TL-M-083", "price", 54.99, "stock", 265),
        Map.of("id", "prod-084", "label", "Tool N", "name", "Tool N", "sku", "TL-N-084", "price", 64.99, "stock", 255),
        Map.of("id", "prod-085", "label", "Tool O", "name", "Tool O", "sku", "TL-O-085", "price", 74.99, "stock", 245),
        Map.of("id", "prod-086", "label", "Tool P", "name", "Tool P", "sku", "TL-P-086", "price", 84.99, "stock", 235),
        Map.of("id", "prod-087", "label", "Tool Q", "name", "Tool Q", "sku", "TL-Q-087", "price", 94.99, "stock", 225),
        Map.of("id", "prod-088", "label", "Tool R", "name", "Tool R", "sku", "TL-R-088", "price", 104.99, "stock", 215),
        Map.of("id", "prod-089", "label", "Tool S", "name", "Tool S", "sku", "TL-S-089", "price", 114.99, "stock", 205),
        Map.of("id", "prod-090", "label", "Tool T", "name", "Tool T", "sku", "TL-T-090", "price", 124.99, "stock", 195),
        Map.of("id", "prod-091", "label", "Device A", "name", "Device A", "sku", "DVC-A-091", "price", 1299.99, "stock", 30),
        Map.of("id", "prod-092", "label", "Device B", "name", "Device B", "sku", "DVC-B-092", "price", 1399.99, "stock", 25),
        Map.of("id", "prod-093", "label", "Device C", "name", "Device C", "sku", "DVC-C-093", "price", 1499.99, "stock", 20),
        Map.of("id", "prod-094", "label", "Device D", "name", "Device D", "sku", "DVC-D-094", "price", 1599.99, "stock", 15),
        Map.of("id", "prod-095", "label", "Device E", "name", "Device E", "sku", "DVC-E-095", "price", 1699.99, "stock", 35),
        Map.of("id", "prod-096", "label", "Device F", "name", "Device F", "sku", "DVC-F-096", "price", 1799.99, "stock", 28),
        Map.of("id", "prod-097", "label", "Device G", "name", "Device G", "sku", "DVC-G-097", "price", 1899.99, "stock", 22),
        Map.of("id", "prod-098", "label", "Device H", "name", "Device H", "sku", "DVC-H-098", "price", 1999.99, "stock", 18),
        Map.of("id", "prod-099", "label", "Device I", "name", "Device I", "sku", "DVC-I-099", "price", 2099.99, "stock", 12),
        Map.of("id", "prod-100", "label", "Device J", "name", "Device J", "sku", "DVC-J-100", "price", 2199.99, "stock", 8),
        Map.of("id", "prod-101", "label", "Component A", "name", "Component A", "sku", "CMP-A-101", "price", 24.99, "stock", 500),
        Map.of("id", "prod-102", "label", "Component B", "name", "Component B", "sku", "CMP-B-102", "price", 29.99, "stock", 480),
        Map.of("id", "prod-103", "label", "Component C", "name", "Component C", "sku", "CMP-C-103", "price", 34.99, "stock", 460),
        Map.of("id", "prod-104", "label", "Component D", "name", "Component D", "sku", "CMP-D-104", "price", 39.99, "stock", 440),
        Map.of("id", "prod-105", "label", "Component E", "name", "Component E", "sku", "CMP-E-105", "price", 44.99, "stock", 520),
        Map.of("id", "prod-106", "label", "Component F", "name", "Component F", "sku", "CMP-F-106", "price", 49.99, "stock", 490),
        Map.of("id", "prod-107", "label", "Component G", "name", "Component G", "sku", "CMP-G-107", "price", 54.99, "stock", 470),
        Map.of("id", "prod-108", "label", "Component H", "name", "Component H", "sku", "CMP-H-108", "price", 59.99, "stock", 450),
        Map.of("id", "prod-109", "label", "Component I", "name", "Component I", "sku", "CMP-I-109", "price", 64.99, "stock", 430),
        Map.of("id", "prod-110", "label", "Component J", "name", "Component J", "sku", "CMP-J-110", "price", 69.99, "stock", 510),
        Map.of("id", "prod-111", "label", "Component K", "name", "Component K", "sku", "CMP-K-111", "price", 19.99, "stock", 550),
        Map.of("id", "prod-112", "label", "Component L", "name", "Component L", "sku", "CMP-L-112", "price", 22.99, "stock", 530),
        Map.of("id", "prod-113", "label", "Component M", "name", "Component M", "sku", "CMP-M-113", "price", 27.99, "stock", 505),
        Map.of("id", "prod-114", "label", "Component N", "name", "Component N", "sku", "CMP-N-114", "price", 32.99, "stock", 485),
        Map.of("id", "prod-115", "label", "Component O", "name", "Component O", "sku", "CMP-O-115", "price", 37.99, "stock", 465),
        Map.of("id", "prod-116", "label", "Component P", "name", "Component P", "sku", "CMP-P-116", "price", 42.99, "stock", 545),
        Map.of("id", "prod-117", "label", "Component Q", "name", "Component Q", "sku", "CMP-Q-117", "price", 47.99, "stock", 525),
        Map.of("id", "prod-118", "label", "Component R", "name", "Component R", "sku", "CMP-R-118", "price", 52.99, "stock", 515),
        Map.of("id", "prod-119", "label", "Component S", "name", "Component S", "sku", "CMP-S-119", "price", 57.99, "stock", 495),
        Map.of("id", "prod-120", "label", "Component T", "name", "Component T", "sku", "CMP-T-120", "price", 62.99, "stock", 475),
        Map.of("id", "prod-121", "label", "Accessory A", "name", "Accessory A", "sku", "ACC-A-121", "price", 14.99, "stock", 600),
        Map.of("id", "prod-122", "label", "Accessory B", "name", "Accessory B", "sku", "ACC-B-122", "price", 16.99, "stock", 580),
        Map.of("id", "prod-123", "label", "Accessory C", "name", "Accessory C", "sku", "ACC-C-123", "price", 18.99, "stock", 560),
        Map.of("id", "prod-124", "label", "Accessory D", "name", "Accessory D", "sku", "ACC-D-124", "price", 20.99, "stock", 540),
        Map.of("id", "prod-125", "label", "Accessory E", "name", "Accessory E", "sku", "ACC-E-125", "price", 22.99, "stock", 620),
        Map.of("id", "prod-126", "label", "Accessory F", "name", "Accessory F", "sku", "ACC-F-126", "price", 24.99, "stock", 590),
        Map.of("id", "prod-127", "label", "Accessory G", "name", "Accessory G", "sku", "ACC-G-127", "price", 26.99, "stock", 570),
        Map.of("id", "prod-128", "label", "Accessory H", "name", "Accessory H", "sku", "ACC-H-128", "price", 28.99, "stock", 550),
        Map.of("id", "prod-129", "label", "Accessory I", "name", "Accessory I", "sku", "ACC-I-129", "price", 30.99, "stock", 530),
        Map.of("id", "prod-130", "label", "Accessory J", "name", "Accessory J", "sku", "ACC-J-130", "price", 32.99, "stock", 610),
        Map.of("id", "prod-131", "label", "Accessory K", "name", "Accessory K", "sku", "ACC-K-131", "price", 12.99, "stock", 650),
        Map.of("id", "prod-132", "label", "Accessory L", "name", "Accessory L", "sku", "ACC-L-132", "price", 13.99, "stock", 630),
        Map.of("id", "prod-133", "label", "Accessory M", "name", "Accessory M", "sku", "ACC-M-133", "price", 15.99, "stock", 605),
        Map.of("id", "prod-134", "label", "Accessory N", "name", "Accessory N", "sku", "ACC-N-134", "price", 17.99, "stock", 585),
        Map.of("id", "prod-135", "label", "Accessory O", "name", "Accessory O", "sku", "ACC-O-135", "price", 19.99, "stock", 565),
        Map.of("id", "prod-136", "label", "Accessory P", "name", "Accessory P", "sku", "ACC-P-136", "price", 21.99, "stock", 645),
        Map.of("id", "prod-137", "label", "Accessory Q", "name", "Accessory Q", "sku", "ACC-Q-137", "price", 23.99, "stock", 625),
        Map.of("id", "prod-138", "label", "Accessory R", "name", "Accessory R", "sku", "ACC-R-138", "price", 25.99, "stock", 615),
        Map.of("id", "prod-139", "label", "Accessory S", "name", "Accessory S", "sku", "ACC-S-139", "price", 27.99, "stock", 595),
        Map.of("id", "prod-140", "label", "Accessory T", "name", "Accessory T", "sku", "ACC-T-140", "price", 29.99, "stock", 575),
        Map.of("id", "prod-141", "label", "Premium A", "name", "Premium A", "sku", "PRM-A-141", "price", 2499.99, "stock", 5),
        Map.of("id", "prod-142", "label", "Premium B", "name", "Premium B", "sku", "PRM-B-142", "price", 2599.99, "stock", 4),
        Map.of("id", "prod-143", "label", "Premium C", "name", "Premium C", "sku", "PRM-C-143", "price", 2699.99, "stock", 3),
        Map.of("id", "prod-144", "label", "Premium D", "name", "Premium D", "sku", "PRM-D-144", "price", 2799.99, "stock", 6),
        Map.of("id", "prod-145", "label", "Premium E", "name", "Premium E", "sku", "PRM-E-145", "price", 2899.99, "stock", 7),
        Map.of("id", "prod-146", "label", "Premium F", "name", "Premium F", "sku", "PRM-F-146", "price", 2999.99, "stock", 2),
        Map.of("id", "prod-147", "label", "Premium G", "name", "Premium G", "sku", "PRM-G-147", "price", 3099.99, "stock", 8),
        Map.of("id", "prod-148", "label", "Premium H", "name", "Premium H", "sku", "PRM-H-148", "price", 3199.99, "stock", 9),
        Map.of("id", "prod-149", "label", "Premium I", "name", "Premium I", "sku", "PRM-I-149", "price", 3299.99, "stock", 1),
        Map.of("id", "prod-150", "label", "Premium J", "name", "Premium J", "sku", "PRM-J-150", "price", 3399.99, "stock", 10)
    );
}
