import { Button } from "@/core/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/core/components/ui/card";
import { MockAlertNotificationData } from "@/price-alerts/components/price-alerts-notifications-grid";
import { BadgeCheck, Clock, ShoppingCart, Trash2 } from "lucide-react";
import Link from "next/link";

interface PriceAlertNotificationCardProps {
  alertNotificationData: MockAlertNotificationData;
}

export const PriceAlertNotificationCard = ({
  alertNotificationData,
}: PriceAlertNotificationCardProps) => {
  const formattedPrice = new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: alertNotificationData.notification.currency,
  }).format(alertNotificationData.notification.price);

  const notificationDate = new Date(alertNotificationData.notification.date);
  const formattedNotificationDate = new Intl.DateTimeFormat("en-US", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(notificationDate);

  return (
    <Card className="w-[22rem]">
      <CardHeader>
        {/* FIXME: add image host to config and use Image component */}
        <img
          src={alertNotificationData.mainImageUrl}
          alt={alertNotificationData.name}
        />

        <CardTitle className="text-2xl">
          <Link href={`/produkty/${alertNotificationData.id}`}>
            {alertNotificationData.name}
          </Link>
        </CardTitle>

        <CardDescription className="flex items-center justify-between">
          <span>{alertNotificationData.category}</span>
          <span>EAN: {alertNotificationData.ean}</span>
        </CardDescription>
      </CardHeader>

      <CardContent className="space-y-1">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <ShoppingCart className="h-5 w-5" />
            <span className="font-medium">
              {alertNotificationData.notification.shop}
            </span>
          </div>

          <div className="text-2xl font-bold text-primary">
            {formattedPrice}
          </div>
        </div>

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <BadgeCheck className="h-5 w-5" />
            <span className="capitalize">
              {alertNotificationData.notification.condition}
            </span>
          </div>

          <div className="flex items-center gap-2 text-sm">
            <Clock className="h-5 w-5" />
            <span>{formattedNotificationDate}</span>
          </div>
        </div>
      </CardContent>

      <CardFooter className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {alertNotificationData.isAvailable ? (
            <>
              <span className="h-2 w-2 rounded-full bg-green-500" />
              <span className="text-sm text-green-500">Dostępny</span>
            </>
          ) : (
            <>
              <span className="h-2 w-2 rounded-full bg-red-500" />
              <span className="text-sm text-red-500">Niedostępny</span>
            </>
          )}
        </div>

        <Button variant="destructive">
          <Trash2 /> Usuń z historii
        </Button>
      </CardFooter>
    </Card>
  );
};
