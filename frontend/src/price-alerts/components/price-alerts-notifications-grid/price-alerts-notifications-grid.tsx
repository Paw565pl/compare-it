import { DeleteConfirmationAlertDialog } from "@/core/components";
import { H1 } from "@/core/components/ui/h1";
import { PriceAlertNotificationCard } from "@/price-alerts/components";

export interface MockAlertNotificationData {
  id: string;
  name: string;
  ean: string;
  category: string;
  mainImageUrl: string;
  isAvailable: boolean;
  notification: {
    date: string;
    shop: string;
    price: number;
    condition: string;
    currency: string;
  };
}

const mockData: MockAlertNotificationData[] = [
  {
    id: "678ab25c83090f04acb0013d",
    name: "Karta graficzna AMD Radeon RX 6950 XT 16GB GDDR6 256bit",
    ean: "0727419314039",
    category: "Karty graficzne",
    mainImageUrl:
      "https://f00.esfr.pl/foto/7/110797139161/9bad5fb2150e761ba420685cf121417c/amd-karta-graf-amd-rx-6950-xt-16g-gddr6,110797139161_3.webp",
    isAvailable: true,
    notification: {
      date: "2023-10-15T14:30:00Z",
      shop: "Amazon",
      price: 299.99,
      condition: "New",
      currency: "PLN",
    },
  },
  {
    id: "678ab25c83090f04acb0013e",
    name: "Karta graficzna Gigabyte GeForce RTX 4060 EAGLE OC 8GB GDDR6 128bit DLSS 3",
    ean: "4719331313708",
    category: "Karty graficzne",
    mainImageUrl:
      "https://f00.esfr.pl/foto/7/127317650537/a77f59fb1c72c9cfd1487e5516807c39/gigabyte-geforce-rtx-4060-4060-eagle-oc-8gb-gddr6-128bit,127317650537_3.webp",
    isAvailable: true,
    notification: {
      date: "2023-10-14T09:15:00Z",
      shop: "Best Buy",
      price: 189.95,
      condition: "Refurbished",
      currency: "PLN",
    },
  },
  {
    id: "678ab25c83090f04acb0013f",
    name: "Karta graficzna ASrock Radeon RX 6400 Challenger ITX 4GB GDDR6 64bit FSR",
    ean: "4710483936944",
    category: "Karty graficzne",
    mainImageUrl:
      "https://f00.esfr.pl/foto/8/119884696825/dbffcd612d41a54cda0be200b9876713/asrock-radeon-rx-6400-challenger-itx-4gb-gddr6-64bit,119884696825_3.webp",
    isAvailable: false,
    notification: {
      date: "2023-10-13T16:45:00Z",
      shop: "Walmart",
      price: 799.0,
      condition: "Open Box",
      currency: "PLN",
    },
  },
  {
    id: "678ab25c83090f04acb0013fa",
    name: "Karta graficzna ASrock Radeon RX 6400 Challenger ITX 4GB GDDR6 64bit FSR",
    ean: "4710483936944",
    category: "Karty graficzne",
    mainImageUrl:
      "https://f00.esfr.pl/foto/8/119884696825/dbffcd612d41a54cda0be200b9876713/asrock-radeon-rx-6400-challenger-itx-4gb-gddr6-64bit,119884696825_3.webp",
    isAvailable: true,
    notification: {
      date: "2023-10-12T11:20:00Z",
      shop: "Target",
      price: 159.5,
      condition: "Used",
      currency: "PLN",
    },
  },
];

export const PriceAlertsNotificationsGrid = () => {
  return (
    <>
      <H1>Twoja historia alertów cenowych</H1>

      <DeleteConfirmationAlertDialog
        alertDialogTriggerLabel={"Wyczyść całą historię powiadomień"}
        alertDialogTriggerClassName="mb-4"
        handleDelete={() => console.log("delete price all alert notifications")}
      />

      <section className="flex flex-wrap justify-center gap-2 sm:justify-start">
        {mockData.map((alertNotificationData, i) => (
          <PriceAlertNotificationCard
            alertNotificationData={alertNotificationData}
            key={i}
          />
        ))}
      </section>
    </>
  );
};
