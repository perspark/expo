// Copyright 2015-present 650 Industries. All rights reserved.

#import "EXLinkingManager.h"
#import "EXScopedModuleRegistry.h"

#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTUtils.h>

NSString * const EXLinkingEventOpenUrl = @"url";

@interface EXLinkingManager ()

@property (nonatomic, weak) id <EXLinkingManagerScopedModuleDelegate> kernelLinkingDelegate;
@property (nonatomic, strong) NSURL *initialUrl;
@property (nonatomic) BOOL hasListeners;

@end

@implementation EXLinkingManager

EX_EXPORT_SCOPED_MODULE(RCTLinkingManager, KernelLinkingManager);

- (instancetype)initWithExperienceId:(NSString *)experienceId kernelServiceDelegate:(id)kernelServiceInstance params:(NSDictionary *)params
{
  if (self = [super initWithExperienceId:experienceId kernelServiceDelegate:kernelServiceInstance params:params]) {
    _kernelLinkingDelegate = kernelServiceInstance;
    _initialUrl = params[@"initialUri"];
  }
  return self;
}

#pragma mark - RCTEventEmitter methods

- (NSArray<NSString *> *)supportedEvents
{
  return @[EXLinkingEventOpenUrl];
}

- (void)startObserving
{
  _hasListeners = YES;
}

- (void)stopObserving
{
  _hasListeners = NO;
}

#pragma mark - Linking methods

- (void)dispatchOpenUrlEvent:(NSURL *)url
{
  if (!url || !url.absoluteString) {
    RCTFatal(RCTErrorWithMessage([NSString stringWithFormat:@"Tried to open a deep link to an invalid url: %@", url]));
    return;
  }
  if (_hasListeners) {
    [self sendEventWithName:EXLinkingEventOpenUrl body:@{@"url": url.absoluteString}];
  }
}

RCT_EXPORT_METHOD(openURL:(NSURL *)URL
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
  if ([self _isExponentUrl:URL]) {
    [_kernelLinkingDelegate linkingModule:self didOpenUrl:URL.absoluteString];
    resolve(@YES);
  } else {
    BOOL opened = [RCTSharedApplication() openURL:URL];
    if (opened) {
      resolve(nil);
    } else {
      reject(RCTErrorUnspecified, [NSString stringWithFormat:@"Unable to open URL: %@", URL], nil);
    }
  }
}

RCT_EXPORT_METHOD(canOpenURL:(NSURL *)URL
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject)
{
  BOOL canOpen = [self _isExponentUrl:URL];
  if (!canOpen) {
    canOpen = [RCTSharedApplication() canOpenURL:URL];
  }
  resolve(@(canOpen));
}

RCT_EXPORT_METHOD(getInitialURL:(RCTPromiseResolveBlock)resolve
                  reject:(__unused RCTPromiseRejectBlock)reject)
{
  resolve(RCTNullIfNil(_initialUrl.absoluteString));
}

# pragma mark - internal

- (BOOL)_isExponentUrl: (NSURL *)url
{
  // do not attempt to route internal exponent links at all if we're in a detached exponent app.
  NSString *versionsPath = [[NSBundle mainBundle] pathForResource:@"EXSDKVersions" ofType:@"plist"];
  NSDictionary *versionsConfig = (versionsPath) ? [NSDictionary dictionaryWithContentsOfFile:versionsPath] : [NSDictionary dictionary];
  if (versionsConfig && versionsConfig[@"detachedNativeVersions"]) {
    return NO;
  }
  
  // we don't need to explicitly include a shell app custom URL scheme here
  // because the default iOS linking behavior will still hand those links back to Exponent.

  NSURLComponents *components = [NSURLComponents componentsWithURL:url resolvingAgainstBaseURL:YES];
  if (components) {
    return ([components.scheme isEqualToString:@"exp"] ||
            [components.scheme isEqualToString:@"exps"] ||
            [components.host isEqualToString:@"exp.host"] ||
            [components.host hasSuffix:@".exp.host"]
            );
  }
  return NO;
}

@end
