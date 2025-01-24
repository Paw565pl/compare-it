import { H1 } from "@/core/components/ui/h1";
import { PriceAlertCard } from "@/profile/components/price-alert-card";

export interface MockAlertData {
  id: string;
  name: string;
  ean: string;
  category: string;
  mainImageUrl: string;
  lowestCurrentPrice: number;
  lowestPriceShop: string;
  offerCount: number;
  isAvailable: boolean;
  currency: string;
  alert: {
    desiredPrice: number;
    desiredCondition: string;
    desiredCurrency: string;
  };
}

const mockData: MockAlertData[] = [
  {
    id: "678ab25c83090f04acb0013d",
    name: "Karta graficzna AMD Radeon RX 6950 XT 16GB GDDR6 256bit",
    ean: "0727419314039",
    category: "Karty graficzne",
    mainImageUrl:
      "https://f00.esfr.pl/foto/7/110797139161/9bad5fb2150e761ba420685cf121417c/amd-karta-graf-amd-rx-6950-xt-16g-gddr6,110797139161_3.webp",
    lowestCurrentPrice: 2359,
    lowestPriceShop: "RTV Euro AGD",
    offerCount: 1,
    isAvailable: true,
    currency: "PLN",
    alert: {
      desiredCurrency: "PLN",
      desiredPrice: 200,
      desiredCondition: "Nowy",
    },
  },
  {
    id: "678ab25c83090f04acb0013e",
    name: "Karta graficzna Gigabyte GeForce RTX 4060 EAGLE OC 8GB GDDR6 128bit DLSS 3",
    ean: "4719331313708",
    category: "Karty graficzne",
    mainImageUrl:
      "https://f00.esfr.pl/foto/7/127317650537/a77f59fb1c72c9cfd1487e5516807c39/gigabyte-geforce-rtx-4060-4060-eagle-oc-8gb-gddr6-128bit,127317650537_3.webp",
    lowestCurrentPrice: 1499,
    lowestPriceShop: "RTV Euro AGD",
    offerCount: 1,
    isAvailable: true,
    currency: "PLN",
    alert: {
      desiredCurrency: "PLN",
      desiredPrice: 200,
      desiredCondition: "Nowy",
    },
  },
  {
    id: "678ab25c83090f04acb0013f",
    name: "Karta graficzna ASrock Radeon RX 6400 Challenger ITX 4GB GDDR6 64bit FSR",
    ean: "4710483936944",
    category: "Karty graficzne",
    mainImageUrl:
      "https://f00.esfr.pl/foto/8/119884696825/dbffcd612d41a54cda0be200b9876713/asrock-radeon-rx-6400-challenger-itx-4gb-gddr6-64bit,119884696825_3.webp",
    lowestCurrentPrice: 699,
    lowestPriceShop: "RTV Euro AGD",
    offerCount: 1,
    isAvailable: true,
    currency: "PLN",
    alert: {
      desiredCurrency: "PLN",
      desiredPrice: 200,
      desiredCondition: "Nowy",
    },
  },
  {
    id: "678ab25c83090f04acb0013f",
    name: "Karta graficzna ASrock Radeon RX 6400 Challenger ITX 4GB GDDR6 64bit FSR",
    ean: "4710483936944",
    category: "Karty graficzne",
    mainImageUrl:
      "https://f00.esfr.pl/foto/8/119884696825/dbffcd612d41a54cda0be200b9876713/asrock-radeon-rx-6400-challenger-itx-4gb-gddr6-64bit,119884696825_3.webp",
    lowestCurrentPrice: 699,
    lowestPriceShop: "RTV Euro AGD",
    offerCount: 1,
    isAvailable: true,
    currency: "PLN",
    alert: {
      desiredCurrency: "PLN",
      desiredPrice: 200,
      desiredCondition: "Nowy",
    },
  },
];

export const PriceAlertsGrid = () => {
  return (
    <>
      <H1>Twoje alerty cenowe</H1>

      <section className="grid grid-cols-[repeat(auto-fill,minmax(350px,1fr))] justify-center justify-items-center gap-4 sm:justify-start sm:justify-items-start">
        {mockData.map((alertData, i) => (
          <PriceAlertCard alertData={alertData} key={i} />
        ))}
      </section>
    </>
  );
};
