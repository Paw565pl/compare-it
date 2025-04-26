import { OfferEntity } from "@/products/entities/offer-entity";

type ShopLowestPriceByDate = Record<string, Record<string, number>>;

interface ChartDataPoint {
  date: string;
  [shopName: string]: number | string | null;
}

export const convertPriceDataToChartFormat = (
  offers: OfferEntity[],
): ChartDataPoint[] => {
  const flatPriceStamps = offers.flatMap((offer) =>
    offer.priceHistory.map((stamp) => ({
      shop: offer.shop,
      price: stamp.price,
      date: stamp.timestamp.split("T")[0],
    })),
  );

  const shopLowestPriceByDate = flatPriceStamps.reduce<ShopLowestPriceByDate>(
    (acc, { date, shop, price }) => {
      if (!acc[date]) acc[date] = {};
      const existingPrice = acc[date][shop];

      if (!existingPrice || price < existingPrice) acc[date][shop] = price;

      return acc;
    },
    {},
  );

  const allShopNames = offers.map((offer) => offer.shop);
  const dates = Object.keys(shopLowestPriceByDate).toSorted(
    (a, b) => new Date(a).getTime() - new Date(b).getTime(),
  );

  const chartData: ChartDataPoint[] = dates.map((date) => {
    const pricesForDate = shopLowestPriceByDate[date] || {};
    const shopPrices = Object.fromEntries(
      allShopNames.map((shopName) => [
        shopName,
        pricesForDate[shopName] ?? null,
      ]),
    );

    return { date, ...shopPrices };
  });

  return chartData;
};
