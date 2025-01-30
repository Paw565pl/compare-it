import { ShopImage } from "@/products/components/index";
import { formatCurrency } from "@/products/utils/format-currency";

interface SingleOfferProps {
  readonly shop: string;
  readonly shopLogoUrl: string;
  readonly url: string;
  readonly priceHistory: PriceHistoryProps[];
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
  const formattedPrice = formatCurrency(
    priceHistory[priceHistory.length - 1].price,
    priceHistory[priceHistory.length - 1].currency,
  );

  return (
    <div className="flex items-center justify-between bg-white p-4">
      <ShopImage name={shop} imageUrl={shopLogoUrl} />
      <div className="text-lg font-semibold text-secondary">
        {formattedPrice}
      </div>
    </div>
  );
};

export { SingleOffer };
