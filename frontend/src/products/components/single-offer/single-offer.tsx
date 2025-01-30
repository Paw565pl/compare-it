import { ShopImage } from "@/products/components/index";

interface SingleOfferProps {
  readonly shop: string;
  readonly shopLogoUrl: string;
  readonly url: string;
  readonly priceHistory: PriceHistoryProps;
}

interface PriceHistoryProps {
  readonly timestamp: string;
  readonly price: number;
  readonly currency: string;
  readonly promoCode: boolean;
  readonly isAvailable: boolean;
  readonly condition: string;
}

const SingleOffer = ({
  shop,
  shopLogoUrl,
  url,
  priceHistory,
}: SingleOfferProps) => {
  return (
    <div className="bg-white p-4">
      <ShopImage name={shop} imageUrl={shopLogoUrl} />
      Product
    </div>
  );
};

export { SingleOffer };
