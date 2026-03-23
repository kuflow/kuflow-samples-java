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

import com.kuflow.temporal.activity.datasource.model.DataSourceItem;
import java.util.List;
import java.util.Map;

/**
 * Mock product data for testing data source pagination.
 */
public final class DataSourceMocks {

    private DataSourceMocks() {
        // Utility class
    }

    public static final List<DataSourceItem> MOCK_PRODUCTS = List.of(
        item("prod-001", "Widget A", "Widget A", "WDG-A-001", 99.99, 150),
        item("prod-002", "Widget B", "Widget B", "WDG-B-002", 149.99, 75),
        item("prod-003", "Widget C", "Widget C", "WDG-C-003", 199.99, 120),
        item("prod-004", "Widget D", "Widget D", "WDG-D-004", 89.99, 200),
        item("prod-005", "Widget E", "Widget E", "WDG-E-005", 129.99, 95),
        item("prod-006", "Widget F", "Widget F", "WDG-F-006", 159.99, 110),
        item("prod-007", "Widget G", "Widget G", "WDG-G-007", 179.99, 85),
        item("prod-008", "Widget H", "Widget H", "WDG-H-008", 139.99, 140),
        item("prod-009", "Widget I", "Widget I", "WDG-I-009", 109.99, 165),
        item("prod-010", "Widget J", "Widget J", "WDG-J-010", 119.99, 180),
        item("prod-011", "Widget K", "Widget K", "WDG-K-011", 94.99, 155),
        item("prod-012", "Widget L", "Widget L", "WDG-L-012", 104.99, 125),
        item("prod-013", "Widget M", "Widget M", "WDG-M-013", 114.99, 145),
        item("prod-014", "Widget N", "Widget N", "WDG-N-014", 124.99, 135),
        item("prod-015", "Widget O", "Widget O", "WDG-O-015", 134.99, 115),
        item("prod-016", "Widget P", "Widget P", "WDG-P-016", 144.99, 105),
        item("prod-017", "Widget Q", "Widget Q", "WDG-Q-017", 154.99, 95),
        item("prod-018", "Widget R", "Widget R", "WDG-R-018", 164.99, 85),
        item("prod-019", "Widget S", "Widget S", "WDG-S-019", 174.99, 175),
        item("prod-020", "Widget T", "Widget T", "WDG-T-020", 184.99, 165),
        item("prod-021", "Widget U", "Widget U", "WDG-U-021", 194.99, 155),
        item("prod-022", "Widget V", "Widget V", "WDG-V-022", 79.99, 145),
        item("prod-023", "Widget W", "Widget W", "WDG-W-023", 69.99, 135),
        item("prod-024", "Widget X", "Widget X", "WDG-X-024", 59.99, 125),
        item("prod-025", "Widget Y", "Widget Y", "WDG-Y-025", 49.99, 115),
        item("prod-026", "Widget Z", "Widget Z", "WDG-Z-026", 209.99, 105),
        item("prod-027", "Widget AA", "Widget AA", "WDG-AA-027", 219.99, 95),
        item("prod-028", "Widget AB", "Widget AB", "WDG-AB-028", 229.99, 185),
        item("prod-029", "Widget AC", "Widget AC", "WDG-AC-029", 239.99, 175),
        item("prod-030", "Widget AD", "Widget AD", "WDG-AD-030", 249.99, 165),
        item("prod-031", "Widget AE", "Widget AE", "WDG-AE-031", 259.99, 155),
        item("prod-032", "Widget AF", "Widget AF", "WDG-AF-032", 269.99, 145),
        item("prod-033", "Widget AG", "Widget AG", "WDG-AG-033", 279.99, 135),
        item("prod-034", "Widget AH", "Widget AH", "WDG-AH-034", 289.99, 125),
        item("prod-035", "Widget AI", "Widget AI", "WDG-AI-035", 299.99, 115),
        item("prod-036", "Widget AJ", "Widget AJ", "WDG-AJ-036", 84.99, 190),
        item("prod-037", "Widget AK", "Widget AK", "WDG-AK-037", 74.99, 170),
        item("prod-038", "Widget AL", "Widget AL", "WDG-AL-038", 64.99, 160),
        item("prod-039", "Widget AM", "Widget AM", "WDG-AM-039", 54.99, 150),
        item("prod-040", "Widget AN", "Widget AN", "WDG-AN-040", 44.99, 140),
        item("prod-041", "Widget AO", "Widget AO", "WDG-AO-041", 189.99, 130),
        item("prod-042", "Widget AP", "Widget AP", "WDG-AP-042", 169.99, 120),
        item("prod-043", "Widget AQ", "Widget AQ", "WDG-AQ-043", 159.99, 110),
        item("prod-044", "Widget AR", "Widget AR", "WDG-AR-044", 149.99, 100),
        item("prod-045", "Widget AS", "Widget AS", "WDG-AS-045", 139.99, 90),
        item("prod-046", "Widget AT", "Widget AT", "WDG-AT-046", 129.99, 195),
        item("prod-047", "Widget AU", "Widget AU", "WDG-AU-047", 119.99, 185),
        item("prod-048", "Widget AV", "Widget AV", "WDG-AV-048", 109.99, 175),
        item("prod-049", "Widget AW", "Widget AW", "WDG-AW-049", 99.99, 165),
        item("prod-050", "Widget AX", "Widget AX", "WDG-AX-050", 89.99, 155),
        item("prod-051", "Gadget A", "Gadget A", "GDG-A-051", 299.99, 80),
        item("prod-052", "Gadget B", "Gadget B", "GDG-B-052", 349.99, 65),
        item("prod-053", "Gadget C", "Gadget C", "GDG-C-053", 399.99, 90),
        item("prod-054", "Gadget D", "Gadget D", "GDG-D-054", 449.99, 55),
        item("prod-055", "Gadget E", "Gadget E", "GDG-E-055", 499.99, 70),
        item("prod-056", "Gadget F", "Gadget F", "GDG-F-056", 549.99, 45),
        item("prod-057", "Gadget G", "Gadget G", "GDG-G-057", 599.99, 60),
        item("prod-058", "Gadget H", "Gadget H", "GDG-H-058", 649.99, 35),
        item("prod-059", "Gadget I", "Gadget I", "GDG-I-059", 699.99, 50),
        item("prod-060", "Gadget J", "Gadget J", "GDG-J-060", 749.99, 25),
        item("prod-061", "Gadget K", "Gadget K", "GDG-K-061", 799.99, 40),
        item("prod-062", "Gadget L", "Gadget L", "GDG-L-062", 849.99, 30),
        item("prod-063", "Gadget M", "Gadget M", "GDG-M-063", 899.99, 20),
        item("prod-064", "Gadget N", "Gadget N", "GDG-N-064", 949.99, 15),
        item("prod-065", "Gadget O", "Gadget O", "GDG-O-065", 999.99, 10),
        item("prod-066", "Gadget P", "Gadget P", "GDG-P-066", 319.99, 85),
        item("prod-067", "Gadget Q", "Gadget Q", "GDG-Q-067", 369.99, 75),
        item("prod-068", "Gadget R", "Gadget R", "GDG-R-068", 419.99, 95),
        item("prod-069", "Gadget S", "Gadget S", "GDG-S-069", 469.99, 100),
        item("prod-070", "Gadget T", "Gadget T", "GDG-T-070", 519.99, 65),
        item("prod-071", "Tool A", "Tool A", "TL-A-071", 39.99, 250),
        item("prod-072", "Tool B", "Tool B", "TL-B-072", 49.99, 230),
        item("prod-073", "Tool C", "Tool C", "TL-C-073", 59.99, 210),
        item("prod-074", "Tool D", "Tool D", "TL-D-074", 69.99, 190),
        item("prod-075", "Tool E", "Tool E", "TL-E-075", 79.99, 270),
        item("prod-076", "Tool F", "Tool F", "TL-F-076", 89.99, 240),
        item("prod-077", "Tool G", "Tool G", "TL-G-077", 99.99, 220),
        item("prod-078", "Tool H", "Tool H", "TL-H-078", 109.99, 200),
        item("prod-079", "Tool I", "Tool I", "TL-I-079", 119.99, 180),
        item("prod-080", "Tool J", "Tool J", "TL-J-080", 129.99, 260),
        item("prod-081", "Tool K", "Tool K", "TL-K-081", 34.99, 290),
        item("prod-082", "Tool L", "Tool L", "TL-L-082", 44.99, 280),
        item("prod-083", "Tool M", "Tool M", "TL-M-083", 54.99, 265),
        item("prod-084", "Tool N", "Tool N", "TL-N-084", 64.99, 255),
        item("prod-085", "Tool O", "Tool O", "TL-O-085", 74.99, 245),
        item("prod-086", "Tool P", "Tool P", "TL-P-086", 84.99, 235),
        item("prod-087", "Tool Q", "Tool Q", "TL-Q-087", 94.99, 225),
        item("prod-088", "Tool R", "Tool R", "TL-R-088", 104.99, 215),
        item("prod-089", "Tool S", "Tool S", "TL-S-089", 114.99, 205),
        item("prod-090", "Tool T", "Tool T", "TL-T-090", 124.99, 195),
        item("prod-091", "Device A", "Device A", "DVC-A-091", 1299.99, 30),
        item("prod-092", "Device B", "Device B", "DVC-B-092", 1399.99, 25),
        item("prod-093", "Device C", "Device C", "DVC-C-093", 1499.99, 20),
        item("prod-094", "Device D", "Device D", "DVC-D-094", 1599.99, 15),
        item("prod-095", "Device E", "Device E", "DVC-E-095", 1699.99, 35),
        item("prod-096", "Device F", "Device F", "DVC-F-096", 1799.99, 28),
        item("prod-097", "Device G", "Device G", "DVC-G-097", 1899.99, 22),
        item("prod-098", "Device H", "Device H", "DVC-H-098", 1999.99, 18),
        item("prod-099", "Device I", "Device I", "DVC-I-099", 2099.99, 12),
        item("prod-100", "Device J", "Device J", "DVC-J-100", 2199.99, 8),
        item("prod-101", "Component A", "Component A", "CMP-A-101", 24.99, 500),
        item("prod-102", "Component B", "Component B", "CMP-B-102", 29.99, 480),
        item("prod-103", "Component C", "Component C", "CMP-C-103", 34.99, 460),
        item("prod-104", "Component D", "Component D", "CMP-D-104", 39.99, 440),
        item("prod-105", "Component E", "Component E", "CMP-E-105", 44.99, 520),
        item("prod-106", "Component F", "Component F", "CMP-F-106", 49.99, 490),
        item("prod-107", "Component G", "Component G", "CMP-G-107", 54.99, 470),
        item("prod-108", "Component H", "Component H", "CMP-H-108", 59.99, 450),
        item("prod-109", "Component I", "Component I", "CMP-I-109", 64.99, 430),
        item("prod-110", "Component J", "Component J", "CMP-J-110", 69.99, 510),
        item("prod-111", "Component K", "Component K", "CMP-K-111", 19.99, 550),
        item("prod-112", "Component L", "Component L", "CMP-L-112", 22.99, 530),
        item("prod-113", "Component M", "Component M", "CMP-M-113", 27.99, 505),
        item("prod-114", "Component N", "Component N", "CMP-N-114", 32.99, 485),
        item("prod-115", "Component O", "Component O", "CMP-O-115", 37.99, 465),
        item("prod-116", "Component P", "Component P", "CMP-P-116", 42.99, 545),
        item("prod-117", "Component Q", "Component Q", "CMP-Q-117", 47.99, 525),
        item("prod-118", "Component R", "Component R", "CMP-R-118", 52.99, 515),
        item("prod-119", "Component S", "Component S", "CMP-S-119", 57.99, 495),
        item("prod-120", "Component T", "Component T", "CMP-T-120", 62.99, 475),
        item("prod-121", "Accessory A", "Accessory A", "ACC-A-121", 14.99, 600),
        item("prod-122", "Accessory B", "Accessory B", "ACC-B-122", 16.99, 580),
        item("prod-123", "Accessory C", "Accessory C", "ACC-C-123", 18.99, 560),
        item("prod-124", "Accessory D", "Accessory D", "ACC-D-124", 20.99, 540),
        item("prod-125", "Accessory E", "Accessory E", "ACC-E-125", 22.99, 620),
        item("prod-126", "Accessory F", "Accessory F", "ACC-F-126", 24.99, 590),
        item("prod-127", "Accessory G", "Accessory G", "ACC-G-127", 26.99, 570),
        item("prod-128", "Accessory H", "Accessory H", "ACC-H-128", 28.99, 550),
        item("prod-129", "Accessory I", "Accessory I", "ACC-I-129", 30.99, 530),
        item("prod-130", "Accessory J", "Accessory J", "ACC-J-130", 32.99, 610),
        item("prod-131", "Accessory K", "Accessory K", "ACC-K-131", 12.99, 650),
        item("prod-132", "Accessory L", "Accessory L", "ACC-L-132", 13.99, 630),
        item("prod-133", "Accessory M", "Accessory M", "ACC-M-133", 15.99, 605),
        item("prod-134", "Accessory N", "Accessory N", "ACC-N-134", 17.99, 585),
        item("prod-135", "Accessory O", "Accessory O", "ACC-O-135", 19.99, 565),
        item("prod-136", "Accessory P", "Accessory P", "ACC-P-136", 21.99, 645),
        item("prod-137", "Accessory Q", "Accessory Q", "ACC-Q-137", 23.99, 625),
        item("prod-138", "Accessory R", "Accessory R", "ACC-R-138", 25.99, 615),
        item("prod-139", "Accessory S", "Accessory S", "ACC-S-139", 27.99, 595),
        item("prod-140", "Accessory T", "Accessory T", "ACC-T-140", 29.99, 575),
        item("prod-141", "Premium A", "Premium A", "PRM-A-141", 2499.99, 5),
        item("prod-142", "Premium B", "Premium B", "PRM-B-142", 2599.99, 4),
        item("prod-143", "Premium C", "Premium C", "PRM-C-143", 2699.99, 3),
        item("prod-144", "Premium D", "Premium D", "PRM-D-144", 2799.99, 6),
        item("prod-145", "Premium E", "Premium E", "PRM-E-145", 2899.99, 7),
        item("prod-146", "Premium F", "Premium F", "PRM-F-146", 2999.99, 2),
        item("prod-147", "Premium G", "Premium G", "PRM-G-147", 3099.99, 8),
        item("prod-148", "Premium H", "Premium H", "PRM-H-148", 3199.99, 9),
        item("prod-149", "Premium I", "Premium I", "PRM-I-149", 3299.99, 1),
        item("prod-150", "Premium J", "Premium J", "PRM-J-150", 3399.99, 10)
    );

    private static DataSourceItem item(String id, String label, String name, String sku, double price, int stock) {
        return DataSourceItem.of(id, Map.of("label", label, "name", name, "sku", sku, "price", price, "stock", stock));
    }
}
